package dev.getelements.elements.sdk.model.oidc;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * The form parameters of a token endpoint request. Only the {@code authorization_code} and
 * {@code refresh_token} grant types are supported.
 */
@Schema(description = "The parameters of an OIDC provider token request.")
public class OidcProviderTokenRequest {

    @Schema(description = "The grant type: \"authorization_code\" or \"refresh_token\".")
    @NotBlank
    private String grantType;

    @Schema(description = "The authorization code, when granting with authorization_code.")
    private String code;

    @Schema(description = "The redirect URI, when granting with authorization_code. Must match the authorize request.")
    private String redirectUri;

    @Schema(description = "The client identifier. Required unless supplied via HTTP basic authentication.")
    private String clientId;

    @Schema(description = "The client secret. Required unless supplied via HTTP basic authentication.")
    private String clientSecret;

    @Schema(description = "The PKCE code verifier, when granting with authorization_code.")
    private String codeVerifier;

    @Schema(description = "The refresh token, when granting with refresh_token.")
    private String refreshToken;

    @Schema(description = "The requested scope, when granting with client_credentials. Defaults to the " +
            "client's registered scopes when omitted.")
    private String scope;

    @Schema(description = "The requested audience (aud), when granting with client_credentials. Must be one " +
            "of the client's registered audiences (or the client id when none are registered).")
    private String audience;

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
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

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public void setCodeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

}
