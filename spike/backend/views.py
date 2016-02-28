import json

from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse
from django.views.decorators.http import require_POST
from backend.models import User

# HTTP Return Codes
HTTP_OK = 200
HTTP_BAD_REQUEST = 400
HTTP_UNAUTHORISED = 401
HTTP_INTERNAL_SERVER_ERROR = 500

# Application Error codes
APP_MALFORMED_JSON = 1
APP_MISSING_DATA = 2


def genJsonResponse(json_string, return_code=200):
    return HttpResponse(content=json_string, status=return_code, content_type="application/json")


# Create your views here.
def index(request):
    """
    This one is when you need to smile
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
    json_data = None

    try:
        json_data = json.loads(request.body.decode('utf-8'))
    except ValueError:
        return genJsonResponse(json.dumps({"error": {"code": APP_MALFORMED_JSON, "message": "Malformed JSON"}}),
                               status=HTTP_BAD_REQUEST)

    if "phone" not in json_data or "first_name" not in json_data or "last_name" not in json_data\
            or "pin" not in json_data:
        return HttpResponse(json.dumps({"error": {"code": APP_MISSING_DATA, "message": "Incomplete JSON"}}),
                            status=HTTP_BAD_REQUEST)

    #TODO Validation, how does it works?
    newUser = User(phone=json_data["phone"], first_name=json_data["first_name"], last_name=json_data["last_name"],
                   pin=json_data["pin"])
    newUser.save()

    return HttpResponse(json.dumps({"success": True}))


def userLogin(request):
    """ Login a user in order to create a session.

    :param request: the request that contains values.
    :return: the http response to the client.
    """

    return HttpResponse(", ".join([str(t) for t in User.objects.all()]))


@require_POST
def alertGetlist(request):
    """ Get a list of the avtive alerts.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    return HttpResponse("alertGetlist")


@require_POST
def alertGet(request):
    """ Get a specific alert by id.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    return HttpResponse("alertGet")


@require_POST
def alertAdd(request):
    """ Add an alert to the currently active alerts.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    return HttpResponse("alertAdd")


@require_POST
def alertUpdate(request):
    """ Update an existing alert.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    return HttpResponse("alertUpdate")


@require_POST
def alertClose(request):
    """ Close an alert which has the effect of setting it inactive.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    return HttpResponse("alertClose")


@require_POST
def alertMyalerts(request):
    """ Get a list of all the alerts created by the current user.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    return HttpResponse("alertMyalerts")
