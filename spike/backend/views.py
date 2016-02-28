from django.shortcuts import render
from django.http import HttpResponse
import json


# Create your views here.
def index(request):
    d = {"title": "Hello !",
         "content": 'My name is Pinkie Pie "Hello!" ; and I am here to say "What\'s up ?" ; I\'m gonna make you smile!'}
    return HttpResponse(json.dumps(d))


def userRegister(request):
    return HttpResponse("userRegister")

def userLogin(request):
    return HttpResponse("userLogin")

def alertGetlist(request):
    return HttpResponse("alertGetlist")

def alertGet(request):
    return HttpResponse("alertGet")

def alertAdd(request):
    return HttpResponse("alertAdd")

def alertUpdate(request):
    return HttpResponse("alertUpdate")

def alertClose(request):
    return HttpResponse("alertClose")

def alertMyalerts(request):
    return HttpResponse("alertMyalerts")