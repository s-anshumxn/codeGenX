from rest_framework import serializers

from utils import BaseSerializer


class DecompositionSerializer(BaseSerializer):
    decomposition_validation = serializers.CharField(required=False)