from django.contrib import admin
from backend.models import User, Alert, Session, Vote, PositionHistory


# Register your models here.

class VoteInline(admin.TabularInline):
    model = Vote
    extra = 1


class PositionHistoryInline(admin.TabularInline):
    model = PositionHistory
    extra = 0


@admin.register(User)
class UserAdmin(admin.ModelAdmin):
    readonly_fields = ['view_position']
    fieldsets = [
        ("Identity", {'fields': [('first_name', 'last_name'), ('mail', 'phone')]}),
        ('User data', {'fields': ['pin', 'view_position']}),
        ("Position", {'fields': ['last_position', ], 'classes': ['collapse']}),
    ]
    list_display = ['id', 'mail', 'first_name', 'last_name', 'view_position']
    search_fields = ['mail', 'first_name', 'last_name']

    def view_position(self, obj):
        return "({}, {})".format(obj.last_position.x, obj.last_position.y)

    view_position.short_description = "Position"


@admin.register(Alert)
class AlertAdmin(admin.ModelAdmin):
    readonly_fields = ('getNbVotes',)
    fieldsets = [
        ("Alert data", {'fields': ['name', ('author', 'isActive'), 'getNbVotes']}),
        ('Position', {'fields': ['alert_position'], 'classes': ['collapse']}),
    ]
    list_display = ['id', 'name', 'author', 'getAlertPosition', 'getNbVotes', 'getScore', 'isActive']
    search_fields = ['name', 'author__first_name', 'author__last_name']
    list_filter = ['isActive']
    inlines = [VoteInline]


@admin.register(Session)
class SessionAdmin(admin.ModelAdmin):
    fieldsets = [
        (None, {'fields': ['id', 'user', 'expiration']})
    ]
    list_display = ['id', 'user', 'expiration']
    search_fields = ['id', 'user']


@admin.register(PositionHistory)
class PositionHistoryAdmin(admin.ModelAdmin):
    readonly_fields = ('timestamp', 'view_position')
    fieldsets = [
        ("Data", {'fields': ['user', 'timestamp', 'view_position']}),
        ('Position', {'fields': ['position'], 'classes': ['collapse']}),
    ]
    list_display = ['id', 'user', 'timestamp', 'getPosition']
    search_fields = ['id', 'user__first_name', 'user__last_name']
    list_filter = ('timestamp',)

    date_hierarchy = 'timestamp'

    def view_position(self, obj):
        return "({}, {})".format(obj.position.x, obj.position.y)

    view_position.short_description = "Position"
