import uuid
from functools import reduce

from django.contrib.gis.db import models
from django.contrib.gis.geos import GEOSGeometry
from django.core import validators


# Create your models here.

class PositionHistory(models.Model):
    """ Represents an history of all the user's last positions.
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    user = models.ForeignKey('User', on_delete=models.CASCADE)
    timestamp = models.DateTimeField(auto_now_add=True)
    position = models.PointField(srid=4326)

    def getPosition(self):
        return "({}, {})".format(self.position.x, self.position.y)

    getPosition.short_description = ("Position")

    def __str__(self):
        return "{} {} ({}, {})".format(self.user, self.timestamp, self.position.x, self.position.y)


class User(models.Model):
    """ User class represents a user of the application.
    """
    # Identification values
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    pin = models.CharField(max_length=4)

    # Personal values
    first_name = models.CharField(max_length=50)
    last_name = models.CharField(max_length=50)
    phone = models.CharField("Phone number", max_length=12, unique=True,
                             validators=[validators.RegexValidator(regex=r"^\+3{2}\d{9}|\d{10}$")])
    mail = models.CharField(max_length=512, unique=True, validators=[validators.EmailValidator])

    #  Srid 4326 is compatible with longitude and latitude provided by Google's API
    last_position = models.PointField(srid=4326, null=True)
    radius = models.FloatField(default=10000.0)

    def updatePosition(self, lon, lat):
        # Persist the last position
        if self.last_position:
            p = PositionHistory(user=self, position=self.last_position)
            p.save()
        # Than change the current
        pnt = GEOSGeometry('SRID=4326;POINT({} {})'.format(lon, lat))
        self.last_position = pnt
        self.full_clean()
        self.save()

    def __str__(self):
        return "{} {}".format(self.first_name, self.last_name)


class Alert(models.Model):
    """ Represents an alert on a specific position.
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    author = models.ForeignKey('User', on_delete=models.CASCADE)
    name = models.CharField(max_length=50)

    #  Srid 4326 is compatible with longitude and latitude provided by Google's API
    alert_position = models.PointField(srid=4326)
    # longitude = models.DecimalField(max_digits=9, decimal_places=6)
    # latitude = models.DecimalField(max_digits=9, decimal_places=6)

    isActive = models.BooleanField("Is active", default=True)

    def distance(self, position):
        """ Compute the distance between the current alert and a given position.

        :param position a point with "x" and "y" fields set to a gps coordinate.
        :return: the distance between the current alert and the given position.
        """
        pnt = GEOSGeometry('SRID=4326;POINT({} {})'.format(self.alert_position.x, self.alert_position.y))
        pnt2 = GEOSGeometry('SRID=4326;POINT({} {})'.format(position.x, position.y))
        return pnt.distance(pnt2)

    def getScore(self):
        return reduce(lambda x, y: x + y, map(lambda x: 1 if x.value else -1, self.vote_set.all()), 0)

    getScore.short_description = 'Score'

    def getNbVotes(self):
        return self.vote_set.count()

    getNbVotes.short_description = "Nb of votes"

    def getAlertPosition(self):
        return "({}, {})".format(self.alert_position.x, self.alert_position.y)

    getAlertPosition.short_description = "Position"

    def __str__(self):
        return "{} by {} at ({},{})".format(self.name, self.author, self.alert_position.x, self.alert_position.y)


class Session(models.Model):
    """ Represents the sessions inside the application
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    expiration = models.DateTimeField(editable=False)
    user = models.ForeignKey('User', on_delete=models.CASCADE)

    def __str__(self):
        return "{} for {} expires at {}".format(self.id, self.user, self.expiration)


class Vote(models.Model):
    """ Represents the votes for a given alert
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    user = models.ForeignKey('User', on_delete=models.CASCADE)
    value = models.BooleanField()
    alert = models.ForeignKey(Alert, on_delete=models.CASCADE)
