from rest_framework import serializers

from utils import BaseSerializer


class DecompositionValidationSerializer(BaseSerializer):
    decomposition = serializers.CharField()