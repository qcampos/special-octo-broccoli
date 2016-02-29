import json

from django.core.exceptions import ValidationError
from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse
from django.views.decorators.http import require_POST
from functools import reduce
from datetime import datetime, timedelta

from backend.models import User, Alert, Session

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


# Classic answers
def answerMalformedJson():
    """ Return a valid http answer to inform the user that the received json was malformed, with the right error code
    and a potentially more specific message.

    :return: the HttpReponse object fully initialized.
    """
    return genJsonResponse(json.dumps({"error": {"code": APP_MALFORMED_JSON, "message": "Malformed JSON"}}),
                           return_code=HTTP_BAD_REQUEST)


def answerIncompleteJson():
    """ Return a valid http answer to inform the user that the received json was missing some keys, with the right error
     code and a potentially more specific message.

    :return: the HttpReponse object fully initialized.
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
    return genJsonResponse(json.dumps({"error": {"code": APP_SESSION_NOT_FOUND, "message": "Session not found"}}))


def answerSessionExpired():
    """ Return a valid Http answer to inform the user of the expiration of his session

    :return: the HttpResponse object fully initialized
    """
    return genJsonResponse(json.dumps({"error": {"code": APP_SESSION_EXPIRED, "message": "Session expired"}}))


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
        if session.expiration < datetime.now():
            session.delete()
            raise SessionCheckFail(answerSessionExpired())
        return session.user
    except Session.DoesNotExist:
        raise SessionCheckFail(answerSessionNotFound())


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
        # This needs to be done with the two methods, each checks something
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

    # Creating the session with an expireation time of
    expiration = datetime.now() + timedelta(days=1)
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
    return HttpResponse("alertGetlist")


@csrf_exempt
@require_POST
def alertGet(request):
    """ Get a specific alert by id.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    return HttpResponse("alertGet")


@csrf_exempt
@require_POST
def alertAdd(request):
    """ Add an alert to the currently active alerts.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    return HttpResponse("alertAdd")


@csrf_exempt
@require_POST
def alertClose(request):
    """ Close an alert which has the effect of setting it inactive.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    return HttpResponse("alertClose")


@csrf_exempt
@require_POST
def alertValidate(request):
    """ Votes for an event

    :param request: the HTTP Request
    :return:  the HTTP Response
    """

    return HttpResponse("alertValidate")
