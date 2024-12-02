from rest_framework import serializers

from utils import BaseSerializer


class TransformSerializer(BaseSerializer):
    analysis = serializers.CharField()
    decomposition = serializers.CharField()
    transform_validation = serializers.CharField(required=False)