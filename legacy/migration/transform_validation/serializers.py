from rest_framework import serializers

from utils import BaseSerializer


class TransformValidationSerializer(BaseSerializer):
    analysis = serializers.CharField()
    decomposition = serializers.CharField()
    transform = serializers.CharField()