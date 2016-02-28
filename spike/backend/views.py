import json

from django.http import HttpResponse
from django.views.decorators.http import require_POST
from backend.models import User


# Create your views here.
def index(request):
    """
    :param request:
    :return:
    """
    d = {"title": "Hello !",
         "content": 'My name is Pinkie Pie "Hello!" ; and I am here to say "What\'s up ?" ; I\'m gonna make you smile!'}
    return HttpResponse(json.dumps(d))


def userRegister(request):
    """ Register a new user into the database.

    :param request: the request that contains values.
    :return: the http response to the client.
    """
    u = User(first_name="Nicolas", last_name="Borie", phone="0102030405")
    u.save()
    return HttpResponse("userRegister")


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
