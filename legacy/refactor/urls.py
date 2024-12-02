from django.urls import path
from .views import documentation

urlpatterns = [
    path('documentation/', documentation, name='documentation'),
]