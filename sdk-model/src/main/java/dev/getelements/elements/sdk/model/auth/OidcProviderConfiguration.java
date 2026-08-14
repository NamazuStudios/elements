package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the configuration needed to drive a browser-redirect OIDC authorization code flow for a single
 * provider (e.g. Twitch, Google). This is deliberately separate from {@link OidcAuthScheme}, which only concerns
 * itself with verifying a JWT's signature against an issuer's JWKS. This type holds everything specific to the
 * OAuth2 client role: the client credentials, the scopes and redirect URI registered with the provider, and the
 * provider's discovery document URL. The resolved issuer, authorization endpoint, and token endpoint are never
 * persisted here — they are resolved from {@link #getDiscoveryUrl()} at runtime and cached in memory.
 */
public class OidcProviderConfiguration {

    /** Creates a new instance. */
    public OidcProviderConfiguration() {}

    @NotNull(groups = ValidationGroups.Update.class)
    @Schema(description = "The unique ID of the provider configuration.")
    private String id;

    @NotBlank
    @Schema(description = "A unique, lowercase, URL-safe identifier for the provider (e.g. 'twitch'). " +
            "Used as the 'provider' path segment and request body value for the OIDC session endpoints.")
    private String name;

    @NotBlank
    @Schema(description = "The provider's OIDC discovery document URL, " +
            "e.g. https://id.twitch.tv/oauth2/.well-known/openid-configuration. " +
            "The issuer, authorization endpoint, token endpoint, and JWKS URI are all resolved from this document.")
    private String discoveryUrl;

    @NotBlank
    @Schema(description = "The OAuth2 client id registered with the provider.")
    private String clientId;

    @NotBlank
    @Schema(description = "The OAuth2 client secret registered with the provider. Never returned in API responses.")
    private String clientSecret;

    @Schema(description = "The OAuth2 scopes to request during authorization.")
    private List<String> scopes;

    @NotBlank
    @Schema(description = "The full, literal redirect URI registered with the provider " +
            "(e.g. https://api.example.com/oidc/twitch/callback). Must exactly match the value registered with " +
            "the provider and is reused byte-for-byte in both the authorize request and the token exchange.")
    private String redirectUri;

    @Schema(description = "Additional provider-specific query parameters to include in the authorize request " +
            "(e.g. Twitch's 'claims' parameter to request email/preferred_username in the id_token).")
    private Map<String, String> extraAuthorizeParams;

    @Schema(description = "How the client authenticates to the provider's token endpoint during code exchange.")
    private TokenEndpointAuthMethod tokenEndpointAuthMethod = TokenEndpointAuthMethod.CLIENT_SECRET_BASIC;

    @Schema(description = "Optional. If set, GET /oidc/{provider}/callback redirects the browser here on a " +
            "successful login instead of rendering the default success page. Server-authoritative: not " +
            "overridable per login attempt.")
    private String successRedirectUrl;

    @Schema(description = "Optional. If set, GET /oidc/{provider}/callback redirects the browser here on a " +
            "failed login instead of rendering the default error page. Server-authoritative: not overridable " +
            "per login attempt.")
    private String errorRedirectUrl;

    /**
     * Returns the unique ID of the provider configuration.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the provider configuration.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the unique provider identifier.
     *
     * @return the provider identifier
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique provider identifier.
     *
     * @param name the provider identifier
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the provider's OIDC discovery document URL.
     *
     * @return the discovery URL
     */
    public String getDiscoveryUrl() {
        return discoveryUrl;
    }

    /**
     * Sets the provider's OIDC discovery document URL.
     *
     * @param discoveryUrl the discovery URL
     */
    public void setDiscoveryUrl(String discoveryUrl) {
        this.discoveryUrl = discoveryUrl;
    }

    /**
     * Returns the OAuth2 client id.
     *
     * @return the client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the OAuth2 client id.
     *
     * @param clientId the client id
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Returns the OAuth2 client secret.
     *
     * @return the client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Sets the OAuth2 client secret.
     *
     * @param clientSecret the client secret
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * Returns the OAuth2 scopes to request.
     *
     * @return the scopes
     */
    public List<String> getScopes() {
        return scopes;
    }

    /**
     * Sets the OAuth2 scopes to request.
     *
     * @param scopes the scopes
     */
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    /**
     * Returns the redirect URI registered with the provider.
     *
     * @return the redirect URI
     */
    public String getRedirectUri() {
        return redirectUri;
    }

    /**
     * Sets the redirect URI registered with the provider.
     *
     * @param redirectUri the redirect URI
     */
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    /**
     * Returns the additional provider-specific authorize query parameters.
     *
     * @return the extra authorize params
     */
    public Map<String, String> getExtraAuthorizeParams() {
        return extraAuthorizeParams;
    }

    /**
     * Sets the additional provider-specific authorize query parameters.
     *
     * @param extraAuthorizeParams the extra authorize params
     */
    public void setExtraAuthorizeParams(Map<String, String> extraAuthorizeParams) {
        this.extraAuthorizeParams = extraAuthorizeParams;
    }

    /**
     * Returns the token endpoint authentication method.
     *
     * @return the token endpoint auth method
     */
    public TokenEndpointAuthMethod getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    /**
     * Sets the token endpoint authentication method.
     *
     * @param tokenEndpointAuthMethod the token endpoint auth method
     */
    public void setTokenEndpointAuthMethod(TokenEndpointAuthMethod tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    /**
     * Returns the configured success redirect URL.
     *
     * @return the success redirect URL, or {@code null} if unset
     */
    public String getSuccessRedirectUrl() {
        return successRedirectUrl;
    }

    /**
     * Sets the success redirect URL.
     *
     * @param successRedirectUrl the success redirect URL
     */
    public void setSuccessRedirectUrl(String successRedirectUrl) {
        this.successRedirectUrl = successRedirectUrl;
    }

    /**
     * Returns the configured error redirect URL.
     *
     * @return the error redirect URL, or {@code null} if unset
     */
    public String getErrorRedirectUrl() {
        return errorRedirectUrl;
    }

    /**
     * Sets the error redirect URL.
     *
     * @param errorRedirectUrl the error redirect URL
     */
    public void setErrorRedirectUrl(String errorRedirectUrl) {
        this.errorRedirectUrl = errorRedirectUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OidcProviderConfiguration that = (OidcProviderConfiguration) o;
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

    @Override
    public String toString() {
        return "OidcProviderConfiguration{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", discoveryUrl='" + discoveryUrl + '\'' +
                ", clientId='" + clientId + '\'' +
                ", scopes=" + scopes +
                ", redirectUri='" + redirectUri + '\'' +
                ", extraAuthorizeParams=" + extraAuthorizeParams +
                ", tokenEndpointAuthMethod=" + tokenEndpointAuthMethod +
                ", successRedirectUrl='" + successRedirectUrl + '\'' +
                ", errorRedirectUrl='" + errorRedirectUrl + '\'' +
                '}';
    }

}
