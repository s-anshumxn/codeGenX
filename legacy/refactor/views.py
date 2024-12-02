import autogen
from autogen import AssistantAgent
from drf_spectacular.utils import extend_schema
from openai import InternalServerError
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.request import Request
from rest_framework.response import Response

from utils import *

config_list_gemini = autogen.config_list_from_json(
            "C:\\Users\\anshuman singh\\Downloads\\fid\\OAI_CONFIG_LIST.json",
            filter_dict={
                "model": ["gemini-pro", "gemini-1.5-pro", "gemini-1.5-pro-001"],
            },
        )

@extend_schema(
    parameters=[usecase_param],
    request={"application/json": BaseSerializer},
    responses=response_dict,
)
@api_view(["POST"])
def documentation(request: Request):
    body = request.data
    serial = BaseSerializer(data=body)
    if not serial.is_valid():
        return Response(
            data={"error": serial.errors}, status=status.HTTP_400_BAD_REQUEST
        )

    params = request.query_params
    match params["usecase"]:
        case "documentation":
            prompt = """
            You are a transformation agent specializing in Java but capable of refactoring code in various languages. Your task is to improve code readability, reduce redundancy, and enhance efficiency using standard refactoring methods. Output only the refactored code.

            [TASKS]
            
            Refactoring:
            -Refactor the provided code to eliminate redundancy and improve readability and efficiency.
            -Use standard refactoring methods applicable to the target language.

            Refactoring Examples (for illustration):
            1 Rename Variables/Methods
             Rename variables, methods, or classes to meaningful and descriptive names to improve code readability. Ensure that all references are updated accordingly.
            2 Extract Method
            -Identify repetitive or large sections of code and extract them into smaller, focused methods with clear names that describe their functionality.
            3 Inline Method
            -Replace a method call with the method's body if the method is trivial or adds unnecessary complexity. Ensure readability is maintained.
            4 Encapsulate Field
            -For any public fields, make them private and create getter and setter methods. This enforces encapsulation and maintains control over how fields are accessed or modified.
            5 Replace Magic Number with Constant
            -Identify hard-coded numbers and replace them with named constants to make their purpose clear and simplify future modifications.
            6 Introduce Parameter Object
            -Combine related parameters passed to a method into a single parameter object. This reduces method complexity and improves code organization.
            7 Replace Conditional with Polymorphism
            -Refactor complex conditional logic by using polymorphism. Replace if-else or switch statements with classes or strategies that encapsulate behavior.
            8 Simplify Conditional Expression
            -Simplify overly complex conditions by consolidating expressions, using helper methods, or applying logical laws (e.g., De Morgan's laws).
            9 Extract Interface
            -Create an interface to define common behavior for classes. Use this interface to promote flexibility and adherence to the Open/Closed Principle.
            10 Remove Dead Code
            -Identify and delete unused or redundant code. Ensure that the removed code does not affect functionality and run tests to verify.

            [CONSTRAINTS]
            
            -Follow coding standards and best practices of the target language.
            -Optimize for readability, maintainability, and efficiency.
            -Output only the refactored code, without any analysis or explanatory text.
            
            """
        case _:
            return Response(
                data={"error": f"{params["usecase"]} is not a valid usecase"},
                status=status.HTTP_400_BAD_REQUEST,
            )

    messages = [{"content": body["code"], "role": "user"}]

    try:
        
        llm_config = {"config_list": config_list_gemini}
        agent = AssistantAgent(
            name="documentation", llm_config=llm_config, system_message=prompt
        )
        response = agent.generate_reply(messages)
        return Response(response["content"], status=status.HTTP_200_OK)
    except InternalServerError:
        return Response(
            data=token_limit_error, status=status.HTTP_500_INTERNAL_SERVER_ERROR
        )
    