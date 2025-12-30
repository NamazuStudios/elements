package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.List;
import java.util.Objects;

public class OAuth2AuthScheme {

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @Schema(description = "The unique ID of the auth scheme.")
    private String id;

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
            then you only need to input "steamid".""")
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValidationUrl() {
        return validationUrl;
    }

    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
    }

    public List<OAuth2RequestKeyValue> getHeaders() {
        return headers;
    }

    public void setHeaders(List<OAuth2RequestKeyValue> headers) {
        this.headers = headers;
    }

    public List<OAuth2RequestKeyValue> getParams() {
        return params;
    }

    public void setParams(List<OAuth2RequestKeyValue> params) {
        this.params = params;
    }

    public List<OAuth2RequestKeyValue> getBody() {
        return body;
    }

    public void setBody(List<OAuth2RequestKeyValue> body) {
        this.body = body;
    }

    public String getResponseIdMapping() {
        return responseIdMapping;
    }

    public void setResponseIdMapping(String responseIdMapping) {
        this.responseIdMapping = responseIdMapping;
    }

    public String getResponseValidMapping() {
        return responseValidMapping;
    }

    public void setResponseValidMapping(String responseValidMapping) {
        this.responseValidMapping = responseValidMapping;
    }

    public String getResponseValidExpectedValue() {
        return responseValidExpectedValue;
    }

    public void setResponseValidExpectedValue(String responseValidExpectedValue) {
        this.responseValidExpectedValue = responseValidExpectedValue;
    }

    public List<Integer> getValidStatusCodes() {
        return validStatusCodes;
    }

    public void setValidStatusCodes(List<Integer> validStatusCodes) {
        this.validStatusCodes = validStatusCodes;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(BodyType bodyType) {
        this.bodyType = bodyType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OAuth2AuthScheme that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(validationUrl, that.validationUrl) && Objects.equals(headers, that.headers) && Objects.equals(params, that.params) && Objects.equals(body, that.body) && Objects.equals(responseIdMapping, that.responseIdMapping) && Objects.equals(responseValidMapping, that.responseValidMapping) && Objects.equals(responseValidExpectedValue, that.responseValidExpectedValue) && Objects.equals(validStatusCodes, that.validStatusCodes) && method == that.method && bodyType == that.bodyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, validationUrl, headers, params, body, responseIdMapping, responseValidMapping, responseValidExpectedValue, validStatusCodes, method, bodyType);
    }

    @Override
    public String toString() {
        return "OAuth2AuthScheme{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
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
