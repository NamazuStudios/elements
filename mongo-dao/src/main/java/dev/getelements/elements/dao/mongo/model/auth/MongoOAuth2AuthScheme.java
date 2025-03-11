package dev.getelements.elements.dao.mongo.model.auth;

import dev.getelements.elements.sdk.model.auth.OAuth2RequestKeyValue;
import dev.morphia.annotations.*;
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
    private String responseIdMapping;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String issuer) {
        this.name = issuer;
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

    public String getResponseIdMapping() {
        return responseIdMapping;
    }

    public void setResponseIdMapping(String responseIdMapping) {
        this.responseIdMapping = responseIdMapping;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoOAuth2AuthScheme that = (MongoOAuth2AuthScheme) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getHeaders(), that.getHeaders()) && Objects.equals(getParams(), that.getParams()) && Objects.equals(getValidationUrl(), that.getValidationUrl()) && Objects.equals(getResponseIdMapping(), that.getResponseIdMapping());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getValidationUrl(), getHeaders(), getParams(), getResponseIdMapping());
    }
}