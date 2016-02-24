from django.shortcuts import render
from django.http import HttpResponse
import json

# Create your views here.
def index(request):
    d = {"title":"Hello !", "content":'My name is Pinkie Pie "Hello!" ; and I am here to say "What\'s up ?" ; I\'m gonna make you smile!'}
    return HttpResponse(json.dumps(d))
