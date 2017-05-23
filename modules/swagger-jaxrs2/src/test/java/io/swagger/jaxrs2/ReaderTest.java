package io.swagger.jaxrs2;

import io.swagger.jaxrs2.resources.*;

import io.swagger.oas.models.ExternalDocumentation;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.PathItem;
import io.swagger.oas.models.callbacks.Callback;
import io.swagger.oas.models.callbacks.Callbacks;
import io.swagger.oas.models.media.Content;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.RequestBody;
import io.swagger.oas.models.responses.ApiResponse;
import io.swagger.oas.models.responses.ApiResponses;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;


public class ReaderTest {
    public static final String EXAMPLE_TAG = "Example tag";
    public static final String OPERATION_SUMMARY = "Operation Summary";
    public static final String OPERATION_DESCRIPTION = "Operation Description";
    public static final String CALLBACK_OPERATION_DESCRIPTION = "payload data will be sent";
    public static final String APPLICATION_JSON = "application/json";
    public static final String RESPONSE_CODE_200 = "200";
    public static final String RESPONSE_CODE_DEFAULT = "default";
    public static final String RESPONSE_DESCRIPTION = "voila!";
    public static final String RESPONSE_DESCRIPTION_BOO = "boo";
    public static final String REQUEST_DESCRIPTION = "Request description";
    public static final String EXTERNAL_DOCS_DESCRIPTION = "External documentation description";
    public static final String EXTERNAL_DOCS_URL = "http://url.com";
    public static final String GENERIC_MEDIA_TYPE = "*/*";
    public static final String PARAMETER_IN = "path";
    public static final String PARAMETER_NAME = "subscriptionId";
    public static final String PARAMETER_DESCRIPTION = "parameter description";
    public static final int RESPONSES_NUMBER = 2;
    public static final int TAG_NUMBER = 1;
    public static final String CALLBACK_SUBSCRIPTION_ID = "subscription";

    private Reader reader;

    @BeforeClass
    public void setup() {
        reader = new Reader((new OpenAPI()));
    }

    @Test(description = "scan methods")
    public void testScanMethods() {
        Method[] methods = SimpleMethods.class.getMethods();
        for (final Method method : methods) {
            if (isValidRestPath(method)) {
                Operation operation = reader.parseMethod(method);
                assertNotNull(operation);
            }
        }
    }

    @Test(description = "Get a Summary and Description")
    public void testGetSummaryAndDescription() {
        Method[] methods = BasicFieldsResource.class.getMethods();
        Operation operation = reader.parseMethod(methods[0]);
        assertNotNull(operation);
        assertEquals(OPERATION_SUMMARY, operation.getSummary());
        assertEquals(OPERATION_DESCRIPTION, operation.getDescription());
    }

    @Test(description = "Deprecated Method")
    public void testDeprecatedMethod() {
        Method[] methods = DeprecatedFieldsResource.class.getMethods();
        Operation deprecatedOperation = reader.parseMethod(methods[0]);
        assertNotNull(deprecatedOperation);
        assertTrue(deprecatedOperation.getDeprecated());
    }

    @Test(description = "Get tags")
    public void testGetTags() {
        Method[] methods = TagsResource.class.getMethods();
        Operation operation = reader.parseMethod(methods[0]);
        assertNotNull(operation);
        assertEquals(TAG_NUMBER, operation.getTags().size());
        assertEquals(EXAMPLE_TAG, operation.getTags().get(0));
    }

    @Test(description = "Responses")
    public void testrGetResponses() {
        Method[] methods = ResponsesResource.class.getMethods();

        Operation responseOperation = reader.parseMethod(methods[0]);
        assertNotNull(responseOperation);
        ApiResponses responses = responseOperation.getResponses();
        assertEquals(RESPONSES_NUMBER, responses.size());

        ApiResponse apiResponse = responses.get(RESPONSE_CODE_200);
        assertNotNull(apiResponse);
        assertEquals(RESPONSE_DESCRIPTION, apiResponse.getDescription());

        Content content = apiResponse.getContent();
        assertNotNull(content);
        assertNotNull(content.get(APPLICATION_JSON));

        apiResponse = responses.get(RESPONSE_CODE_DEFAULT);
        assertNotNull(apiResponse);
        assertEquals(RESPONSE_DESCRIPTION_BOO, apiResponse.getDescription());

        content = apiResponse.getContent();
        assertNotNull(content);
        assertNotNull(content.get(GENERIC_MEDIA_TYPE));
    }

    @Test(description = "Request Body")
    public void testGetRequestBody() {
        Method[] methods = RequestBodyResource.class.getMethods();

        Operation requestOperation = reader.parseMethod(methods[0]);
        assertNotNull(requestOperation);
        RequestBody requestBody = requestOperation.getRequestBody();
        assertEquals(REQUEST_DESCRIPTION, requestBody.getDescription());

        Content content = requestBody.getContent();
        assertNotNull(content);
        assertNotNull(content.get(APPLICATION_JSON));

    }

    @Test(description = "External Docs")
    public void testGetExternalDocs() {
        Method[] methods = ExternalDocsReference.class.getMethods();

        Operation externalDocsOperation = reader.parseMethod(methods[0]);
        assertNotNull(externalDocsOperation);
        ExternalDocumentation externalDocs = externalDocsOperation.getExternalDocs();
        assertEquals(EXTERNAL_DOCS_DESCRIPTION, externalDocs.getDescription());
        assertEquals(EXTERNAL_DOCS_URL, externalDocs.getUrl());
    }

    @Test(description = "Parameters")
    public void testGetParameters() {
        Method[] methods = ParametersResource.class.getMethods();

        Operation parametersOperataion = reader.parseMethod(methods[0]);
        assertNotNull(parametersOperataion);

        List<Parameter> parameters = parametersOperataion.getParameters();
        assertNotNull(parameters);
        assertEquals(1, parameters.size());
        Parameter parameter = parameters.get(0);
        assertNotNull(parameter);
        assertEquals(PARAMETER_IN, parameter.getIn());
        assertEquals(PARAMETER_NAME, parameter.getName());
        assertEquals(PARAMETER_DESCRIPTION, parameter.getDescription());
        assertEquals(Boolean.TRUE, parameter.getRequired());
        assertEquals(Boolean.TRUE, parameter.getAllowEmptyValue());
        assertEquals(Boolean.TRUE, parameter.getAllowReserved());
        assertEquals(Boolean.FALSE, parameter.getDeprecated());
    }


    @Test(description = "Callbacks")
    public void testGetCallbacks() {
        Method[] methods = SimpleCallbackResource.class.getMethods();
        Operation callbackOperation = reader.parseMethod(methods[0]);
        assertNotNull(callbackOperation);
        Callbacks callbacks = callbackOperation.getCallbacks();
        assertNotNull(callbacks);
        Callback callback = callbacks.get(CALLBACK_SUBSCRIPTION_ID);
        assertNotNull(callback);
        PathItem pathItem = callback.get(CALLBACK_SUBSCRIPTION_ID);
        assertNotNull(pathItem);
        Operation postOperation = pathItem.getPost();
        assertNotNull(postOperation);
        assertEquals(CALLBACK_OPERATION_DESCRIPTION, postOperation.getDescription());

        List<Parameter> parameters = postOperation.getParameters();
        assertNotNull(parameters);
        assertEquals(1, parameters.size());
    }

    private Boolean isValidRestPath(Method method) {
        for (Class<? extends Annotation> item : Arrays.asList(GET.class, PUT.class, POST.class, DELETE.class,
                OPTIONS.class, HEAD.class)) {
            if (method.getAnnotation(item) != null) {
                return true;
            }
        }
        return false;
    }
}
