import uuid

from django.contrib.gis.db import models


# Create your models here.

class Alert(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    name = models.CharField(max_length=50)
    lon = models.FloatField()

    def __str__(self):
        return self.id + " : " + self.name


class User(models.Model):
    """ User class represents a user of the application.
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    phone = models.CharField("Phone number", max_length=12)
    first_name = models.CharField(max_length=50)
    last_name = models.CharField(max_length=50)

    def __str__(self):
        return "User({}, {}, {})".format(self.first_name, self.last_name, self.phone)
