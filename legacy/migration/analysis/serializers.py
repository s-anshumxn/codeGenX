from rest_framework import serializers

from utils import BaseSerializer


class AnalysisSerializer(BaseSerializer):
    analysis_validation = serializers.CharField(required=False)