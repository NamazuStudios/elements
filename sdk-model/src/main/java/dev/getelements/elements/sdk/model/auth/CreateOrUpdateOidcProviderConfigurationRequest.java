package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Represents a request to create or update an {@link OidcProviderConfiguration}. */
@Schema(description = "Represents a request to create or update an OIDC provider configuration.")
public class CreateOrUpdateOidcProviderConfigurationRequest {

    /** Creates a new instance. */
    public CreateOrUpdateOidcProviderConfigurationRequest() {}

    @NotBlank
    @Schema(description = "A unique, lowercase, URL-safe identifier for the provider (e.g. 'twitch').")
    private String provider;

    @NotBlank
    @Schema(description = "The provider's OIDC discovery document URL.")
    private String discoveryUrl;

    @NotBlank
    @Schema(description = "The OAuth2 client id registered with the provider.")
    private String clientId;

    @NotBlank(groups = ValidationGroups.Create.class)
    @Schema(description = "The OAuth2 client secret registered with the provider. Required on create; on " +
            "update, leave blank to keep the existing secret unchanged.")
    private String clientSecret;

    @Schema(description = "The OAuth2 scopes to request during authorization.")
    private List<String> scopes;

    @Schema(description = "The full, literal redirect URI registered with the provider. If left blank, " +
            "defaults to this server's built-in OIDC callback URI for the provider " +
            "(i.e. '{this server}/oidc/{provider}/callback').")
    private String redirectUri;

    @Schema(description = "Additional provider-specific query parameters to include in the authorize request.")
    private Map<String, String> extraAuthorizeParams;

    @Schema(description = "How the client authenticates to the provider's token endpoint during code exchange.")
    private TokenEndpointAuthMethod tokenEndpointAuthMethod = TokenEndpointAuthMethod.CLIENT_SECRET_BASIC;

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
        if (!(o instanceof CreateOrUpdateOidcProviderConfigurationRequest that)) return false;
        return Objects.equals(provider, that.provider)
                && Objects.equals(discoveryUrl, that.discoveryUrl)
                && Objects.equals(clientId, that.clientId)
                && Objects.equals(clientSecret, that.clientSecret)
                && Objects.equals(scopes, that.scopes)
                && Objects.equals(redirectUri, that.redirectUri)
                && Objects.equals(extraAuthorizeParams, that.extraAuthorizeParams)
                && tokenEndpointAuthMethod == that.tokenEndpointAuthMethod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, discoveryUrl, clientId, clientSecret, scopes, redirectUri,
                extraAuthorizeParams, tokenEndpointAuthMethod);
    }

    @Override
    public String toString() {
        return "CreateOrUpdateOidcProviderConfigurationRequest{" +
                "provider='" + provider + '\'' +
                ", discoveryUrl='" + discoveryUrl + '\'' +
                ", clientId='" + clientId + '\'' +
                ", scopes=" + scopes +
                ", redirectUri='" + redirectUri + '\'' +
                ", extraAuthorizeParams=" + extraAuthorizeParams +
                ", tokenEndpointAuthMethod=" + tokenEndpointAuthMethod +
                '}';
    }

}
