package dev.getelements.elements.sdk.model.oidc;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The outcome of authenticating on the OIDC provider's hosted login page: either the user is immediately
 * redirected back to the relying party (consent not required), or the provider must first show the consent
 * screen carrying the client's display name and the scopes being requested.
 */
@Schema(description = "The outcome of an OIDC provider hosted-login submission.")
public class OidcProviderConsent {

    @Schema(description = "The pending authorization's opaque id, carried through the consent screen's form.")
    private String authorizationId;

    @Schema(description = "Whether the consent screen must be shown before the authorization completes.")
    private boolean consentRequired;

    @Schema(description = "The relying party's registered display name, shown on the consent screen.")
    private String clientDisplayName;

    @Schema(description = "The scope string as granted, e.g. \"openid profile email\".")
    private String scope;

    @Schema(description = "The redirect URI to send the browser to, present only when consentRequired is false.")
    private String redirectUri;

    public String getAuthorizationId() {
        return authorizationId;
    }

    public void setAuthorizationId(String authorizationId) {
        this.authorizationId = authorizationId;
    }

    public boolean isConsentRequired() {
        return consentRequired;
    }

    public void setConsentRequired(boolean consentRequired) {
        this.consentRequired = consentRequired;
    }

    public String getClientDisplayName() {
        return clientDisplayName;
    }

    public void setClientDisplayName(String clientDisplayName) {
        this.clientDisplayName = clientDisplayName;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

}
