package dev.getelements.elements.dao.mongo.model.auth;

import dev.getelements.elements.sdk.model.auth.TokenEndpointAuthMethod;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity(value = "oidc_provider_configuration", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("provider"), options = @IndexOptions(unique = true))
})
public class MongoOidcProviderConfiguration {

    @Id
    private ObjectId id;

    @Property
    private String provider;

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

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoOidcProviderConfiguration that = (MongoOidcProviderConfiguration) o;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getProvider(), that.getProvider())
                && Objects.equals(getDiscoveryUrl(), that.getDiscoveryUrl())
                && Objects.equals(getClientId(), that.getClientId())
                && Objects.equals(getClientSecret(), that.getClientSecret())
                && Objects.equals(getScopes(), that.getScopes())
                && Objects.equals(getRedirectUri(), that.getRedirectUri())
                && Objects.equals(getExtraAuthorizeParams(), that.getExtraAuthorizeParams())
                && getTokenEndpointAuthMethod() == that.getTokenEndpointAuthMethod();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getProvider(), getDiscoveryUrl(), getClientId(), getClientSecret(),
                getScopes(), getRedirectUri(), getExtraAuthorizeParams(), getTokenEndpointAuthMethod());
    }

}
