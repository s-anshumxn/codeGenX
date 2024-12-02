from rest_framework import serializers

from utils import BaseSerializer


class AnalysisValidationSerializer(BaseSerializer):
    analysis = serializers.CharField(required=False)