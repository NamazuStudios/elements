package dev.getelements.elements.dao.mongo.model.auth;

import dev.getelements.elements.sdk.model.auth.TokenEndpointAuthMethod;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity(value = "oidc_provider_configuration", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("name"), options = @IndexOptions(unique = true))
})
public class MongoOidcProviderConfiguration {

    @Id
    private ObjectId id;

    @Property
    private String name;

    @Property
    private String discoveryUrl;

    @Property
    private String clientId;

    @Property
    private String clientSecret;

    @Property
    private List<String> scopes;

    @Property
    private String redirectUri;

    @Property
    private Map<String, String> extraAuthorizeParams;

    @Property
    private TokenEndpointAuthMethod tokenEndpointAuthMethod = TokenEndpointAuthMethod.CLIENT_SECRET_BASIC;

    @Property
    private String successRedirectUrl;

    @Property
    private String errorRedirectUrl;

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

    public String getDiscoveryUrl() {
        return discoveryUrl;
    }

    public void setDiscoveryUrl(String discoveryUrl) {
        this.discoveryUrl = discoveryUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public Map<String, String> getExtraAuthorizeParams() {
        return extraAuthorizeParams;
    }

    public void setExtraAuthorizeParams(Map<String, String> extraAuthorizeParams) {
        this.extraAuthorizeParams = extraAuthorizeParams;
    }

    public TokenEndpointAuthMethod getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public void setTokenEndpointAuthMethod(TokenEndpointAuthMethod tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    public String getSuccessRedirectUrl() {
        return successRedirectUrl;
    }

    public void setSuccessRedirectUrl(String successRedirectUrl) {
        this.successRedirectUrl = successRedirectUrl;
    }

    public String getErrorRedirectUrl() {
        return errorRedirectUrl;
    }

    public void setErrorRedirectUrl(String errorRedirectUrl) {
        this.errorRedirectUrl = errorRedirectUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoOidcProviderConfiguration that = (MongoOidcProviderConfiguration) o;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getName(), that.getName())
                && Objects.equals(getDiscoveryUrl(), that.getDiscoveryUrl())
                && Objects.equals(getClientId(), that.getClientId())
                && Objects.equals(getClientSecret(), that.getClientSecret())
                && Objects.equals(getScopes(), that.getScopes())
                && Objects.equals(getRedirectUri(), that.getRedirectUri())
                && Objects.equals(getExtraAuthorizeParams(), that.getExtraAuthorizeParams())
                && getTokenEndpointAuthMethod() == that.getTokenEndpointAuthMethod()
                && Objects.equals(getSuccessRedirectUrl(), that.getSuccessRedirectUrl())
                && Objects.equals(getErrorRedirectUrl(), that.getErrorRedirectUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDiscoveryUrl(), getClientId(), getClientSecret(),
                getScopes(), getRedirectUri(), getExtraAuthorizeParams(), getTokenEndpointAuthMethod(),
                getSuccessRedirectUrl(), getErrorRedirectUrl());
    }

}
