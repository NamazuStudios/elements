package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.List;
import java.util.Objects;

/** Represents a request to create or update an OAuth2 Auth Scheme for an Application. */
@Schema(description = "Represents a request to update an Auth Scheme for an Application.")
public class CreateOrUpdateOAuth2AuthSchemeRequest {

    /** Creates a new instance. */
    public CreateOrUpdateOAuth2AuthSchemeRequest() {}

    @NotNull
    @Schema(description = "A unique name used to identify the scheme within the instance of Elements. " +
            "If using the same OAuth2 provider (e.g. Steam), it is recommended to suffix the name for each application " +
            "when using multitenancy, e.g. steam_game1, steam_game2, etc.")
    private String name;

    @NotNull
    @Schema(description = "The URL to send the user token validation request to.")
    private String validationUrl;

    @Schema(description = "The headers required for the validation request.")
    private List<OAuth2RequestKeyValue> headers;

    @Schema(description = "The query parameters required for the validation request.")
    private List<OAuth2RequestKeyValue> params;

    @Schema(description = """
            The body parameters for the validation request when using POST.
            For FORM_URL_ENCODED, these become form fields (key=value&...).""")
    private List<OAuth2RequestKeyValue> body;

    @Schema(description = """
            Determines how to map the user id in the response. Will search the response for the corresponding key. \
            For example, if the response is structured like: \
             {"response": { "params" : { "steamid" : <id> } } } \
            then you only need to input "steamid". \
            Ignored if one of the parameters is marked as user id.""")
    private String responseIdMapping;

    @Schema(description = """
            Optional key in the response whose value indicates whether the token/user is valid.
            For example: "is_valid", "active", or "success".
            If not set, only the HTTP status code is used.
            """)
    private String responseValidMapping;

    @Schema(description = """
            Optional expected value for the validation field.
            If null:
              - boolean true is treated as success
              - non-empty/non-null for non-boolean values is treated as success.
            If set, the field's string value must equal this.
            """)
    private String responseValidExpectedValue;

    @Schema(description = """
            HTTP status codes that are considered "processable" for validation.
            Any other status is treated as failure before inspecting the body.
            Defaults to [200].
            """)
    private List<Integer> validStatusCodes = List.of(200);

    @NotNull
    @Schema(description = "HTTP method for the validation request (GET or POST).")
    private HttpMethod method = HttpMethod.GET;

    @Schema(description = """
            How to encode the request body when using POST.
            FORM_URL_ENCODED corresponds to application/x-www-form-urlencoded.""")
    private BodyType bodyType = BodyType.NONE;

    /**
     * Returns the unique name for the scheme.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name for the scheme.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the URL to send the user token validation request to.
     *
     * @return the validation URL
     */
    public String getValidationUrl() {
        return validationUrl;
    }

    /**
     * Sets the URL to send the user token validation request to.
     *
     * @param validationUrl the validation URL
     */
    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
    }

    /**
     * Returns the headers required for the validation request.
     *
     * @return the headers
     */
    public List<OAuth2RequestKeyValue> getHeaders() {
        return headers;
    }

    /**
     * Sets the headers required for the validation request.
     *
     * @param headers the headers
     */
    public void setHeaders(List<OAuth2RequestKeyValue> headers) {
        this.headers = headers;
    }

    /**
     * Returns the query parameters required for the validation request.
     *
     * @return the query parameters
     */
    public List<OAuth2RequestKeyValue> getParams() {
        return params;
    }

    /**
     * Sets the query parameters required for the validation request.
     *
     * @param params the query parameters
     */
    public void setParams(List<OAuth2RequestKeyValue> params) {
        this.params = params;
    }

    /**
     * Returns the body parameters for the validation request.
     *
     * @return the body parameters
     */
    public List<OAuth2RequestKeyValue> getBody() {
        return body;
    }

    /**
     * Sets the body parameters for the validation request.
     *
     * @param body the body parameters
     */
    public void setBody(List<OAuth2RequestKeyValue> body) {
        this.body = body;
    }

    /**
     * Returns the response field key used to extract the user ID.
     *
     * @return the response ID mapping key
     */
    public String getResponseIdMapping() {
        return responseIdMapping;
    }

    /**
     * Sets the response field key used to extract the user ID.
     *
     * @param responseIdMapping the response ID mapping key
     */
    public void setResponseIdMapping(String responseIdMapping) {
        this.responseIdMapping = responseIdMapping;
    }

    /**
     * Returns the response field key used to determine validity.
     *
     * @return the response valid mapping key
     */
    public String getResponseValidMapping() {
        return responseValidMapping;
    }

    /**
     * Sets the response field key used to determine validity.
     *
     * @param responseValidMapping the response valid mapping key
     */
    public void setResponseValidMapping(String responseValidMapping) {
        this.responseValidMapping = responseValidMapping;
    }

    /**
     * Returns the expected value of the validation field.
     *
     * @return the expected valid value
     */
    public String getResponseValidExpectedValue() {
        return responseValidExpectedValue;
    }

    /**
     * Sets the expected value of the validation field.
     *
     * @param responseValidExpectedValue the expected valid value
     */
    public void setResponseValidExpectedValue(String responseValidExpectedValue) {
        this.responseValidExpectedValue = responseValidExpectedValue;
    }

    /**
     * Returns the HTTP status codes considered processable for validation.
     *
     * @return the valid status codes
     */
    public List<Integer> getValidStatusCodes() {
        return validStatusCodes;
    }

    /**
     * Sets the HTTP status codes considered processable for validation.
     *
     * @param validStatusCodes the valid status codes
     */
    public void setValidStatusCodes(List<Integer> validStatusCodes) {
        this.validStatusCodes = validStatusCodes;
    }

    /**
     * Returns the HTTP method for the validation request.
     *
     * @return the HTTP method
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Sets the HTTP method for the validation request.
     *
     * @param method the HTTP method
     */
    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    /**
     * Returns the body encoding type for POST requests.
     *
     * @return the body type
     */
    public BodyType getBodyType() {
        return bodyType;
    }

    /**
     * Sets the body encoding type for POST requests.
     *
     * @param bodyType the body type
     */
    public void setBodyType(BodyType bodyType) {
        this.bodyType = bodyType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CreateOrUpdateOAuth2AuthSchemeRequest that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(validationUrl, that.validationUrl) && Objects.equals(headers, that.headers) && Objects.equals(params, that.params) && Objects.equals(body, that.body) && Objects.equals(responseIdMapping, that.responseIdMapping) && Objects.equals(responseValidMapping, that.responseValidMapping) && Objects.equals(responseValidExpectedValue, that.responseValidExpectedValue) && Objects.equals(validStatusCodes, that.validStatusCodes) && method == that.method && bodyType == that.bodyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, validationUrl, headers, params, body, responseIdMapping, responseValidMapping, responseValidExpectedValue, validStatusCodes, method, bodyType);
    }

    @Override
    public String toString() {
        return "CreateOrUpdateOAuth2AuthSchemeRequest{" +
                "name='" + name + '\'' +
                ", validationUrl='" + validationUrl + '\'' +
                ", headers=" + headers +
                ", params=" + params +
                ", body=" + body +
                ", responseIdMapping='" + responseIdMapping + '\'' +
                ", responseValidMapping='" + responseValidMapping + '\'' +
                ", responseValidExpectedValue='" + responseValidExpectedValue + '\'' +
                ", validStatusCodes=" + validStatusCodes +
                ", method=" + method +
                ", bodyType=" + bodyType +
                '}';
    }
}
