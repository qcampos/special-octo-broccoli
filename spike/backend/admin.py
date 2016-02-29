from django.contrib import admin
from backend.models import User, Alert


# Register your models here.


class UserAdmin(admin.ModelAdmin):
    fieldsets = [
        (None, {'fields': ['pin']}),
        ('User data', {'fields': ['first_name', 'last_name', 'phone']}),
    ]
    list_display = ['pin', 'first_name', 'last_name', 'id']
    search_fields = ['pin', 'first_name', 'last_name']


class AlertAdmin(admin.ModelAdmin):
    fieldsets = [
        (None, {'fields': ['name', 'author']}),
        ('Position', {'fields': ['position'], 'classes': ['collapse']}),
    ]
    list_display = ['name', 'author', 'position', 'id']
    search_fields = ['name', 'author']


admin.site.register(User, UserAdmin)
admin.site.register(Alert, AlertAdmin)
