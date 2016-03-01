from django.contrib import admin
from backend.models import User, Alert, Session, Vote


# Register your models here.


class UserAdmin(admin.ModelAdmin):
    fieldsets = [
        (None, {'fields': ['first_name', 'last_name', 'mail', 'phone']}),
        ('User data', {'fields': ['pin']}),
    ]
    list_display = ['id', 'mail', 'first_name', 'last_name']
    search_fields = ['mail', 'first_name', 'last_name']


class VoteInline(admin.TabularInline):
    model = Vote
    extra = 1


class AlertAdmin(admin.ModelAdmin):
    fieldsets = [
        (None, {'fields': ['name', 'author', 'isActive']}),
        ('Position', {'fields': ['position'], 'classes': ['collapse']}),
    ]
    list_display = ['id', 'name', 'author', 'getPosition', 'getTotalNote', 'isActive']
    search_fields = ['name', 'author']
    list_filter = ['isActive']
    inlines = [VoteInline]


class SessionAdmin(admin.ModelAdmin):
    fieldsets = [
        (None, {'fields': ['id', 'user', 'expiration']})
    ]
    list_display = ['id', 'user', 'expiration']
    search_fields = ['id', 'user']


admin.site.register(User, UserAdmin)
admin.site.register(Alert, AlertAdmin)
admin.site.register(Session, SessionAdmin)
