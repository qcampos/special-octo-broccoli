import json

from django.core.exceptions import ValidationError
from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse
from django.views.decorators.http import require_POST
from functools import reduce

from backend.models import User, Alert

# HTTP Return Codes
HTTP_OK = 200
HTTP_BAD_REQUEST = 400
HTTP_UNAUTHORISED = 401
HTTP_INTERNAL_SERVER_ERROR = 500

# Application Error codes
APP_MALFORMED_JSON = 1
APP_MISSING_DATA = 2
APP_INVALID_DATA = 3


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


# Create your views here.


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
    # Parse the json string into a dictionary.
    try:
        json_data = json.loads(request.body.decode('utf-8'))
    except ValueError:
        return answerMalformedJson()

    # Check that all expected keys are here.
    requiredKeys = ["phone", "first_name", "last_name", "pin"]
    if not validateJson(json_data, requiredKeys):
        return answerIncompleteJson()

    # Create and check the validity of the user
    newUser = User(phone=json_data["phone"], first_name=json_data["first_name"], last_name=json_data["last_name"],
                   pin=json_data["pin"])
    try:
        # This needs to be done with the two methods, each checks something
        newUser.clean()
        newUser.clean_fields()
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

    return HttpResponse(", ".join([str(t) for t in User.objects.all()]))


@csrf_exempt
@require_POST
def alertGetlist(request):
    """ Get a list of the avtive alerts.

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
def alertUpdate(request):
    """ Update an existing alert.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    return HttpResponse("alertUpdate")


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
def alertMyalerts(request):
    """ Get a list of all the alerts created by the current user.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    return HttpResponse("alertMyalerts")
