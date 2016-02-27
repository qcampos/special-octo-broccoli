from django.contrib.gis.db import models


# Create your models here.

class Alert(models.Model):
    id = models.UUIDField(primary_key=True)
    name = models.CharField(max_length=50)
    lon = models.FloatField()

    def __str__(self):
        return self.id + " : " + self.name

