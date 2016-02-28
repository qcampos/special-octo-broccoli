import uuid

from django.contrib.gis.db import models
from django.contrib.gis.geos import Point, GEOSGeometry


# Create your models here.

class Alert(models.Model):
    """ Represents an alert on a specific position.
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    author = models.ForeignKey('User', on_delete=models.CASCADE)
    name = models.CharField(max_length=50)

    #  Srid 4326 is compatible with longitude and latitude provided by Google's API
    position = models.PointField(srid=4326)

    # longitude = models.DecimalField(max_digits=9, decimal_places=6)
    # latitude = models.DecimalField(max_digits=9, decimal_places=6)

    def distance(self, position):
        """ Compute the distance between the current alert and a given position.

        :param position a point with "x" and "y" fields set to a gps coordinate.
        :return: the distance between the current alert and the given position.
        """
        pnt = GEOSGeometry('SRID=4326;POINT({} {})'.format(self.position.x, self.position.y))
        pnt2 = GEOSGeometry('SRID=4326;POINT({} {})'.format(position.x, position.y))
        return pnt.distance(pnt2) * 100

    def __str__(self):
        return "Alert({}, {}, ({},{}))".format(self.name, self.author, self.position.x, self.position.y)


class User(models.Model):
    """ User class represents a user of the application.
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    phone = models.CharField("Phone number", max_length=12)
    first_name = models.CharField(max_length=50)
    last_name = models.CharField(max_length=50)

    def __str__(self):
        return "User({}, {}, {})".format(self.first_name, self.last_name, self.phone)
