import autogen
from autogen import AssistantAgent
from drf_spectacular.utils import extend_schema
from openai import InternalServerError
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.request import Request
from rest_framework.response import Response

from .serializers import *
from utils import *

config_list_gemini = autogen.config_list_from_json(
    "C:\\Users\\anshuman singh\\Downloads\\fid\\OAI_CONFIG_LIST.json",
    filter_dict={
        "model": ["gemini-pro", "gemini-1.5-pro", "gemini-1.5-pro-001"],
    },
)

@extend_schema(
    parameters=[usecase_param],
    request={"application/json": AnalysisSerializer},
    responses=response_dict,
)
@api_view(["POST"])
def analysis(request: Request):
    body = request.data
    print("analysis input : ",body)
    serial = AnalysisSerializer(data=body)
    if not serial.is_valid():
        # Debugging invalid serializer
        print("Serializer errors:", serial.errors)
        return Response(
            data={"error": serial.errors}, status=status.HTTP_400_BAD_REQUEST
        )

    params = request.query_params
    usecase = params.get("usecase", None)
    if not usecase:
        return Response(
            data={"error": "usecase parameter is missing"}, status=status.HTTP_400_BAD_REQUEST
        )

    match usecase:
        case "soap-to-rest":
            path = os.path.join(prompts_dir, "soap_to_rest")
            with open(os.path.join(path, "analysis.txt")) as infile:
                prompt = infile.read()
        case _:
            return Response(
                data={"error": f"{usecase} is not a valid usecase"},
                status=status.HTTP_400_BAD_REQUEST,
            )

    messages = [{"content": body["prompt"] + body["code"], "role": "user"}]
    if body.get("analysis_validation", None) is not None:
        messages.append({"content": body["analysis_validation"], "role": "assistant"})

    try:
        llm_config = {"config_list": config_list_gemini}
        agent = AssistantAgent(
            name="analysis", llm_config=llm_config, system_message=prompt
        )
        response = agent.generate_reply(messages)
        print("analysis output : ",response["content"])
        return Response(data={"content": response["content"]}, status=status.HTTP_200_OK)
    except InternalServerError:
        return Response(
            data=token_limit_error, status=status.HTTP_500_INTERNAL_SERVER_ERROR
        )
