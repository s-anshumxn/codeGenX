import autogen
from autogen import AssistantAgent
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.request import Request
from rest_framework.response import Response

config_list_gemini = autogen.config_list_from_json(
            "C:\\Users\\anshuman singh\\Downloads\\fid\\OAI_CONFIG_LIST.json",
            filter_dict={
                "model": ["gemini-pro", "gemini-1.5-pro", "gemini-1.5-pro-001"],
            },
        )

@api_view(["POST"])
def refactor(request: Request, *args, **kwargs):
    body = request.data
    
    messages = [{"content" : body["code"], "role": "user"}]
    agent = AssistantAgent(name="refactor", llm_config={"config_list": config_list_gemini}, human_input_mode="NEVER",system_message="""
             [Role] 
                 -You are a Documentation Agent and specialized in Java Language.
                 -You will receive pieces of code snippets to analyze and generate Javadoc comments for.
                 -Your task is to document the Java code you recieve.
             
             [Objective]
                 -You generate comprehensive Javadoc comments for various Java code elements.
                 -Identify the piece of code given consists of what amongst the classes, methods, and fields and produce according to Format for only what's given to you.
                 -Describe the functionality of classes, methods, and fields comprehensively.
                 -You should understand the code context and produce accurate, well-formatted Javadoc comments with appropriate Javadoc tags.
                 -Don't populate the output, try best to breif description and everything.
                 -Ensure to enclose the whole output in a comment.

             [Constraints]
                 -Ensure all generated comments follow standard Javadoc formatting guidelines.
                 -Ensure strictly to not include any piece of code in output just plain text described.
                 -Ensure strictly to not include any improvement of code or any code in output.
                 -Ensure each comment includes appropriate tags such as @param, @return, @throws, @see, etc., where applicable.
                 -The generated comments should be clear, concise, and formatted correctly according to Javadoc standards.

            [Format] 
               Ensure that output will be strictly in format mentioned below->
               ```yaml
                    1. **Class Javadoc Comment:**
                    Generate a Javadoc comment for a Java class with the following details:
                    - Class name: [Class name]
                    - Description: [Brief description of the class]
                    - `@param 1_name`: Include a description of what 1st parameter represents.
                    - `@return`: Describe the return value of the class, if applicable.
                    - `@throws`: Document exceptions that may be thrown by the class.
                    
                    2. **Method Javadoc Comment:**
                    Generate a Javadoc comment for a Java method with the following details:
                    - Method signature: [Method signature]
                    - Description: [Brief description of what the method does]
                    - `@param 1`: Include a description of 1st parameter and what it represents.
                    - `@return`: Describe the return value of the method, if applicable.
                    - `@throws`: Document exceptions that may be thrown by the method.
                    
                    3. **Field Javadoc Comment:**
                    Generate a Javadoc comment for a Java field with the following details:
                    - Field name: [Field name]
                    - Type: [Field type]
                    - Description: [Brief description of the purpose of the field]
                    
                    4. **Constructor Javadoc Comment:**
                    Generate a Javadoc comment for a Java constructor with the following details:
                    - Constructor signature: [Constructor signature]
                    - Description: [Brief description of what the constructor initializes]
                    - `@param 1`: Include a description of 1st parameter and what it initializes.
                    - `@throws`: Document exceptions that may be thrown by the constructor.

                    ```
                Ensure that the generated Javadoc comments are clear, concise, and grammatically correct.
                Use natural language understanding to analyze the provided Java code elements and generate appropriate documentation based on their context.
        """)
    print("REAPI")
    response = agent.generate_reply(messages)
    print(response)
    context= body["code"]+response["content"]
    return Response(response["content"])

# @api_view(["POST"])
# def tryy(request: Request, *args, **kwargs):
    # body = request.data
    # 
    # messages = [{"content" : body["code"], "role": "user"}]
# 
    # llm_config={"config_list": config_list_gemini}
    # agent = AssistantAgent(name="documentation", llm_config=llm_config, human_input_mode="NEVER",system_message="""
            # Role: You are a Documentation Agent and specialized in Java Language. Your task is to document the Java code you recieve.
            # 
            # Objectives:
            # You will do analysis of the Java code you recieve and document it in 3-4 lines what does it do in manaered format that how methods are documented.
            # Return the output as comments as the output will be directly pasted over the code.
            # The code will be pasted in Java editor so make sure to handle \n with newlines.
            # Don't send code just the document and text.
            # Append TRYAPI at the end of output.
        # """)
    # print("TRYAPI")
    # response = agent.generate_reply(messages)
    # context= body["code"]+response["content"]
    # return Response(response["content"])