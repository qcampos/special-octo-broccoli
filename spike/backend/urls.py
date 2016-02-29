from django.conf.urls import url

from . import views

urlpatterns = [
    url(r'^users/register$', views.userRegister, name='userRegister'),
    url(r'^users/login$', views.userLogin, name='userLogin'),
    url(r'^users/check/mail', views.userCheckEmail, name='userCheckEmail'),
    url(r'^users/check/mail', views.userCheckPhone, name='userCheckPhone'),
    url(r'^alerts/getlist$', views.alertGetlist, name='alertGetlist'),
    url(r'^alerts/get$', views.alertGet, name='alertGet'),
    url(r'^alerts/add$', views.alertAdd, name='alertAdd'),
    url(r'^alerts/close$', views.alertClose, name='alertClose'),
]
