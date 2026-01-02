package dev.getelements.elements.dao.mongo.model.auth;

import dev.getelements.elements.sdk.model.auth.BodyType;
import dev.getelements.elements.sdk.model.auth.HttpMethod;
import dev.getelements.elements.sdk.model.auth.OAuth2RequestKeyValue;
import dev.morphia.annotations.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Objects;

@Entity(value = "oauth2_auth_scheme", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("name"), options = @IndexOptions(unique = true))
})
public class MongoOAuth2AuthScheme {

    @Id
    private ObjectId id;

    @Property
    private String name;

    @Property
    private String validationUrl;

    @Property
    private List<OAuth2RequestKeyValue> headers;

    @Property
    private List<OAuth2RequestKeyValue> params;

    @Property
    private List<OAuth2RequestKeyValue> body;

    @Property
    private String responseIdMapping;

    @Property
    private String responseValidMapping;

    @Property
    private String responseValidExpectedValue;

    @Property
    private List<Integer> validStatusCodes;

    @Property
    private HttpMethod method;

    @Property
    private BodyType bodyType;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
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
        if (!(o instanceof MongoOAuth2AuthScheme that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(validationUrl, that.validationUrl) && Objects.equals(headers, that.headers) && Objects.equals(params, that.params) && Objects.equals(body, that.body) && Objects.equals(responseIdMapping, that.responseIdMapping) && Objects.equals(responseValidMapping, that.responseValidMapping) && Objects.equals(responseValidExpectedValue, that.responseValidExpectedValue) && Objects.equals(validStatusCodes, that.validStatusCodes) && method == that.method && bodyType == that.bodyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, validationUrl, headers, params, body, responseIdMapping, responseValidMapping, responseValidExpectedValue, validStatusCodes, method, bodyType);
    }
}