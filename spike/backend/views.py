import json

import datetime
from django.core.exceptions import ValidationError
from django.utils import timezone
from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse
from django.views.decorators.http import require_POST
from functools import reduce
from django.contrib.gis.geos import Point, GEOSGeometry
import uuid
import urllib.request
import urllib.error
import urllib.response
import logging
import threading

from backend.models import User, Alert, Session, Vote

# HTTP Return Codes
HTTP_OK = 200
HTTP_BAD_REQUEST = 400
HTTP_UNAUTHORISED = 401
HTTP_INTERNAL_SERVER_ERROR = 500

# Application Error codes
APP_MALFORMED_JSON = 1
APP_MISSING_DATA = 2
APP_INVALID_DATA = 3
APP_LOGIN_FAILED = 4
APP_SESSION_EXPIRED = 5
APP_SESSION_NOT_FOUND = 6
APP_ALERT_NOT_FOUND = 7
APP_ALERT_ALREADY_VOTED = 8


# Classic answers
def answerMalformedJson():
    """ Return a valid http answer to inform the user that the received json was malformed, with the right error code
    and a potentially more specific message.

    :return: the HttpResponse object fully initialized.
    """
    return genJsonResponse(json.dumps({"error": {"code": APP_MALFORMED_JSON, "message": "Malformed JSON"}}),
                           return_code=HTTP_BAD_REQUEST)


def answerIncompleteJson():
    """ Return a valid http answer to inform the user that the received json was missing some keys, with the right error
     code and a potentially more specific message.

    :return: the HttpResponse object fully initialized.
    """
    return genJsonResponse(json.dumps({"error": {"code": APP_MISSING_DATA, "message": "Incomplete JSON"}}),
                           return_code=HTTP_BAD_REQUEST)


def answerJsonValidation(ve):
    """ Return a valid http answer to inform the user that the received json was invalid at a semantic level because
    some values were not valid.

    :param ve: the ValidationError object witch contains all the error messages.
    :return: the HttpResponse object fully initialized.
    """
    d = ve.message_dict
    return genJsonResponse(json.dumps({"error": {"code": APP_INVALID_DATA, "message": "Error while validating data",
                                                 "data": d}}),
                           return_code=HTTP_BAD_REQUEST)


def answerLoginFailed(message):
    """ Return a valid http answer to inform the user that he couldn't properly login

    :param message: the login explanation of why he couldn't login
    :return: the HttpResponse object fully initialized
    """
    return genJsonResponse(json.dumps({"error": {"code": APP_LOGIN_FAILED, "message": message}}),
                           return_code=HTTP_UNAUTHORISED)


def answerSuccess(success):
    """ Return a valid http answer to inform the user of the success of the operation

    :param success: a boolean indicating success status
    :return: the HttpResponse object fully initialized
    """
    return genJsonResponse(json.dumps({"success": success}))


def answerSession(session, expiration):
    """ Return a valid http answer to inform the user of the attribution of a new session

    :param session: the session id
    :param expiration: the timestamp for the expiration of the session
    :return: the HttpResponse object fully initialized
    """
    return genJsonResponse(json.dumps({"session": str(session), "expire": str(expiration)}))


def answerSessionNotFound():
    """ Return a valid HttpAnswer to inform the user of the fail of his session validation

    :return: the HttpResponse object fully initialized
    """
    return genJsonResponse(json.dumps({"error": {"code": APP_SESSION_NOT_FOUND, "message": "Session not found"}}),
                           return_code=HTTP_UNAUTHORISED)


def answerSessionExpired():
    """ Return a valid Http answer to inform the user of the expiration of his session

    :return: the HttpResponse object fully initialized
    """
    return genJsonResponse(json.dumps({"error": {"code": APP_SESSION_EXPIRED, "message": "Session expired"}}),
                           return_code=HTTP_UNAUTHORISED)


def answerAlertId(alertId):
    """ Return a valid HTTP answer to inform the user of the fact that he successfully created an alert

    :param alertId: the newly created alert id
    :return: the HttpResponse object fully initialized
    """
    return genJsonResponse(json.dumps({"id": str(alertId)}))


def answerAlertNotFound():
    """ Return a valid Http answer to inform the user that the given alert couldn't be found

    :return: the HttpResponse object fully initialized
    """
    return genJsonResponse(json.dumps({"error": {"code": APP_ALERT_NOT_FOUND, "message": "Alert not Found"}}),
                           return_code=HTTP_BAD_REQUEST)


def answerAlreadyVoted():
    """ Return a valid Http answer to inform the user that the given alert couldn't be found

    :return: the HttpResponse object fully initialized
    """
    return genJsonResponse(json.dumps({"error": {"code": APP_ALERT_ALREADY_VOTED, "message": "You already voted"}}),
                           return_code=HTTP_BAD_REQUEST)


def genJsonResponse(json_string, return_code=200):
    """ Create a valid HTTP response containing the given arguments.

    :param json_string: the content of the answer.
    :param return_code: the status code of the answer.
    :return: a HttpResponse object fully initialized.
    """
    return HttpResponse(content=json_string, status=return_code, content_type="application/json")


def validateJson(dictionary, keys):
    """ Ensure that the given dictionary contains all the given keys.

    :param dictionary: the dictionary
    :param keys: a list of hashable objects.
    :return: True is all the objects of the keys list are into the dictionary.
    """
    return reduce(lambda x, y: x and y, [t in dictionary for t in keys])


#########################################
# Refactoring the JSON POST Body parsing
#########################################


class RestMethodInitFail(Exception):
    """ The Exception class used to know when method initialization failed

    When caught, you can use it's field value to grad the HTTPResponse
    generated by the error
    """

    def __init__(self, value):
        self.response = value

    def __str__(self):
        return repr(self.value)


def doInitialChecks(neededValues, request):
    """ Do the initial check for each requests

    :param neededValues: an array containing the required keys that have to be in the json
    :return: a the json data that could be extracted from the request
    :raises: RestMethodInitFail is the needed argument weren't given to the rest Call
    """

    # Parse the json string into a dictionary.
    try:
        json_data = json.loads(request.body.decode('utf-8'))
    except ValueError:
        raise RestMethodInitFail(answerMalformedJson())

    # Check that all expected keys are here.
    if not validateJson(json_data, neededValues):
        raise RestMethodInitFail(answerIncompleteJson())

    return json_data


#####################################
# Refactoring the Session Check Code
#####################################

class SessionCheckFail(Exception):
    """ The exception class thrown when the session check fails
    """

    def __init__(self, value):
        self.response = value

    def __str__(self):
        return repr(self.value)


def checkSession(sessionId):
    """ Checks for the validity of the given session and retrives it's user

    :param sessionId: the id of the session, extracted from the json
    :return: the user associated with the given session
    """
    try:
        session = Session.objects.filter(id=sessionId).get()
        if session.expiration < timezone.make_aware(datetime.datetime.now(), timezone.get_default_timezone()):
            session.delete()
            raise SessionCheckFail(answerSessionExpired())
        return session.user
    except Session.DoesNotExist:
        raise SessionCheckFail(answerSessionNotFound())


############################################################
# Refactoring the part where we make actual API call to GCM
############################################################

# TODO Move to settings?
GCM_API_KEY = "AIzaSyCA0nYK3waZc5VIeCbCHlEtw8NBpjOzzbI"
GCM_API_URL = "https://gcm-http.googleapis.com/gcm/send"


def GCMPostToTopic(topic, data):
    """ Try to send a message to a given topic

    :param topic: a string containg a topic name
    :param data: a dictionnary containg the data
    :return: True if the message could be sent, False if not (and the reason is logged
    """
    data = {
        "to": "/topic/{}".format(topic),
        "data": data,
        "priority": "high"
    }

    data = json.dumps(data).encode('utf-8')
    headers = {
        'Authorization': "key={}".format(GCM_API_KEY),
        'Content-Type': 'application/json',
    }
    req = urllib.request.Request(url=GCM_API_URL, data=data, headers=headers)

    try:
        urllib.request.urlopen(req)
    except urllib.error.HTTPError as err:
        logging.error("HTTP error code {} received when sending messing to topic {}".format(err.code, topic))
        return False
    except urllib.error.URLError as err:
        logging.error("The url {} couldn't be reached".format(GCM_API_URL))
        return False
    logging.info("Successfully sending GCM message to {}".format(topic))
    return True


def notifyNewAlert(al):
    """ Send a notification to every user that is near the given Alert

    :param al: the alert to notify
    """
    users = [usr for usr in User.objects.filter(last_position__isnull=False)
             if al.distance(usr.last_position) < usr.radius]
    logging.info("Notifying {} users for the alert {}".format(len(users), al))

    data = {
        "id": str(al.id),
        "lat": al.alert_position.x,
        "long": al.alert_position.y
    }

    for user in users:
        topic = "user-{}".format(str(user.id))
        GCMPostToTopic(topic,data)


##############################
# Code of the view themselves
##############################


def index(request):
    """ This one is when you need to smile

    :param request:
    :return:
    """
    d = {"title": "Hello !",
         "content": 'My name is Pinkie Pie "Hello!" ; and I am here to say "What\'s up ?" ; I\'m gonna make you smile!'}
    return genJsonResponse(json.dumps(d))


@csrf_exempt
@require_POST
def userRegister(request):
    """ Register a new user into the database.

    :param request: the request that contains values.
    :return: the http response to the client.
    """

    # Getting JSON Data
    try:
        requiredKeys = ["phone", "mail", "first_name", "last_name", "pin"]
        json_data = doInitialChecks(neededValues=requiredKeys, request=request)
    except RestMethodInitFail as fail:
        return fail.response

    # Create and check the validity of the user
    newUser = User(phone=json_data["phone"], first_name=json_data["first_name"], last_name=json_data["last_name"],
                   pin=json_data["pin"], mail=json_data["mail"])
    try:
        newUser.full_clean(validate_unique=True)
    except ValidationError as ve:
        return answerJsonValidation(ve)

    newUser.save()
    return genJsonResponse(json.dumps({"success": True}))


@csrf_exempt
@require_POST
def userLogin(request):
    """ Login a user in order to create a session.

    :param request: the request that contains values.
    :return: the http response to the client.
    """

    # Getting JSON Data
    try:
        requiredKeys = ["login", "pass"]
        json_data = doInitialChecks(neededValues=requiredKeys, request=request)
    except RestMethodInitFail as fail:
        return fail.response

    # Trying to get the user
    try:
        user = User.objects.filter(mail=json_data['login']).get()
    except User.DoesNotExist:
        return answerLoginFailed("User not found")

    # Checking the password
    if user.pin != json_data['pass']:
        return answerLoginFailed("Wrong Pin")

    # Removing old sessions
    for oldSession in Session.objects.filter(user=user):
        oldSession.delete()

    # Creating the session with an expiration time of one day.
    expiration = timezone.make_aware(datetime.datetime.now() + datetime.timedelta(days=1),
                                      timezone.get_default_timezone())
    session = Session(user=user, expiration=expiration)
    session.save()

    return answerSession(session.id, expiration)


@csrf_exempt
@require_POST
def userCheckEmail(request):
    """ Checks if the given email is already used

    :param request: the HTTP Request
    :return: a fully initialized HTTP Response
    """

    # Getting JSON Data
    try:
        requiredKeys = ["mail"]
        json_data = doInitialChecks(neededValues=requiredKeys, request=request)
    except RestMethodInitFail as fail:
        return fail.response

    # If we found an user, we lose
    try:
        User.objects.filter(mail=json_data["mail"]).get()
        return answerSuccess(False)
    except User.DoesNotExist:
        return answerSuccess(True)


@csrf_exempt
@require_POST
def userCheckPhone(request):
    """ Checks if the given phone is already used

    :param request: the HTTP Request
    :return: a fully initialized HTTP Response
    """

    # Getting JSON Data
    try:
        requiredKeys = ["phone"]
        json_data = doInitialChecks(neededValues=requiredKeys, request=request)
    except RestMethodInitFail as fail:
        return fail.response

    # If we found an user, we lose
    try:
        User.objects.filter(phone=json_data["phone"]).get()
        return answerSuccess(False)
    except User.DoesNotExist:
        return answerSuccess(True)


@csrf_exempt
@require_POST
def alertGetlist(request):
    """ Get a list of the active alerts given an user position

    :param request: the request that contains values.
    :return: the http response to the client.
    """

    # Getting JSON Data
    try:
        requiredKeys = ["session", "lat", "long", "radius"]
        json_data = doInitialChecks(neededValues=requiredKeys, request=request)
    except RestMethodInitFail as fail:
        return fail.response

    # Checking session and getting related user
    try:
        user = checkSession(json_data['session'])
    except SessionCheckFail as fail:
        return fail.response

    # Get the data
    latitude = json_data["lat"]
    longitude = json_data["long"]
    radius = float(json_data["radius"])
    # Update the current position of the user while we can.
    user.updatePosition(longitude, latitude)
    user.radius = radius
    user.save()

    # Create the current user point
    point = GEOSGeometry('SRID=4326;POINT({} {})'.format(latitude, longitude))
    # Only get the alerts that are actives and in the radius
    activeAlerts = list(Alert.objects.filter(isActive=True).all())
    distantAlerts = [a for a in activeAlerts if a.distance(point) < user.radius]

    # Make and return the corresponding dictionary
    alerts = [{"id": str(a.id)} for a in distantAlerts]
    return genJsonResponse(json.dumps(alerts))


def jsonAlertRepr(alert, user):
    """ Create a dictionary that contains all the required data to represents an Alert.

    :param alert: the alert to dump.
    :param user: the requiring user, used in the distance computation.
    :return: a dictionary that contains the required data, ready to be dumped into json.
    """
    return {
        "id": str(alert.id),
        "name": alert.name,
        "author": "{} {}".format(alert.author.first_name, alert.author.last_name),
        "long": str(alert.alert_position.x),
        "lat": str(alert.alert_position.y),
        "score": str(alert.getScore()),
        "distance": alert.distance(user.last_position)
    }


@csrf_exempt
@require_POST
def alertGet(request):
    """ Get a specific alert by id.

    :param request: the request that contains values.
    :return: the http response to the client.
    """

    # Getting JSON Data
    try:
        requiredKeys = ["session", "alerts"]
        json_data = doInitialChecks(neededValues=requiredKeys, request=request)
    except RestMethodInitFail as fail:
        return fail.response

    # Checking session and getting related user
    try:
        user = checkSession(json_data['session'])
    except SessionCheckFail as fail:
        return fail.response

    alerts = [t["id"] for t in json_data["alerts"]]
    alertsData = {identifier: jsonAlertRepr(Alert.objects.get(id=identifier), user) for identifier in alerts}
    return genJsonResponse(json.dumps(alertsData))


@csrf_exempt
@require_POST
def alertAdd(request):
    """ Add an alert to the currently active alerts.

    :param request: the request that contains values.
    :return: the http response to the client.
    """

    # Getting JSON Data
    try:
        requiredKeys = ["session", "long", "lat", "name"]
        json_data = doInitialChecks(neededValues=requiredKeys, request=request)
    except RestMethodInitFail as fail:
        return fail.response

    # Checking session and getting related user
    try:
        user = checkSession(json_data['session'])
    except SessionCheckFail as fail:
        return fail.response

    # Creating the alert and checking validity.
    alert = Alert(author=user, name=json_data["name"], alert_position=Point(float(json_data['long']), float(json_data['lat'])))
    try:
        alert.full_clean(validate_unique=True)
    except ValidationError as ve:
        return answerJsonValidation(ve)
    alert.save()

    # Opening a new thread to notify users
    threading.Thread(target=notifyNewAlert, args=[alert], daemon=False).start()
    return answerAlertId(alert.id)


@csrf_exempt
@require_POST
def alertClose(request):
    """ Close an alert which has the effect of setting it inactive.

    :param request: the request that contains values.
    :return: the http response to the client.
    """

    # Getting JSON Data
    try:
        requiredKeys = ["session", "alert"]
        json_data = doInitialChecks(neededValues=requiredKeys, request=request)
    except RestMethodInitFail as fail:
        return fail.response

    # Checking session and getting related user
    try:
        user = checkSession(json_data['session'])
    except SessionCheckFail as fail:
        return fail.response

    # Closing the alert, getting it from the ones that are related to this user
    try:
        alert = user.alert_set.filter(id=uuid.UUID(json_data["alert"])).get()
        alert.isActive = False
        alert.save()
    except Alert.DoesNotExist:
        return answerAlertNotFound()

    # Sending notification that the alert is now closed
    topic = "alert-{}".format(alert.id)
    data = {
        "isActive": False
    }

    # Opening a new thread to notify users
    threading.Thread(target=GCMPostToTopic, args=[topic, data], daemon=False).start()

    return answerSuccess(True)


@csrf_exempt
@require_POST
def alertValidate(request):
    """ Votes for an event

    :param request: the HTTP Request
    :return:  the HTTP Response
    """

    # Getting JSON Data
    try:
        requiredKeys = ["session", "alert", "validate"]
        json_data = doInitialChecks(neededValues=requiredKeys, request=request)
    except RestMethodInitFail as fail:
        return fail.response

    # Checking session and getting related user
    try:
        user = checkSession(json_data['session'])
    except SessionCheckFail as fail:
        return fail.response

    # Getting the alert
    try:
        alert = Alert.objects.filter(id=uuid.UUID(json_data["alert"])).get()
    except Alert.DoesNotExist:
        return answerAlertNotFound()

    # Checking if an vote has already been done
    if len(alert.vote_set.filter(user=user)) > 0:
        return answerAlreadyVoted()

    vote = Vote(user=user, value=json_data['validate'], alert=alert)
    vote.save()

    return answerSuccess(True)
