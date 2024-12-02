import os
from pathlib import Path

from drf_spectacular.types import OpenApiTypes
from drf_spectacular.utils import OpenApiParameter
from rest_framework import serializers


class BaseSerializer(serializers.Serializer):
    code = serializers.CharField()
    prompt = serializers.CharField(required=False)


class ResponseSerializer(serializers.Serializer):
    content = serializers.CharField()


class ErrorSerializer(serializers.Serializer):
    error = serializers.CharField()


parent_dir = Path(__file__).parent
prompts_dir = os.path.join(parent_dir, "prompts")

usecases = ["refactor", "documentation"]
usecase_param = OpenApiParameter(
    name="usecase",
    type=OpenApiTypes.STR,
    location=OpenApiParameter.QUERY,
    required=True,
    enum=usecases,
)

response_dict = {
    (200, "application/json"): ResponseSerializer,
    (400, "application/json"): ErrorSerializer,
    (401, "application/json"): ErrorSerializer,
    (500, "application/json"): ErrorSerializer,
}
access_token_error = {"error": "Invalid access token"}
token_limit_error = {"error": "Context too large, token limit exceeded"}