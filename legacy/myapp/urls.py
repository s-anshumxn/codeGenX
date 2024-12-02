from django.urls import path
from .views import refactor

urlpatterns = [
    path('refactor/', refactor, name='my_api'),
]
