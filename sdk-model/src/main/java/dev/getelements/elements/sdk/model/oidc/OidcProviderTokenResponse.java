package dev.getelements.elements.sdk.model.oidc;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A successful token endpoint response per RFC 6749 section 5.1, carrying the signed {@code id_token}, the
 * Elements session token as the {@code access_token}, and the opaque {@code refresh_token} for rotation.
 */
@Schema(description = "A successful OIDC provider token response.")
public class OidcProviderTokenResponse {

    @Schema(description = "The signed id_token.")
    private String idToken;

    @Schema(description = "The access token: an Elements session token, usable against Elements APIs as a Bearer credential.")
    private String accessToken;

    @Schema(description = "Always \"Bearer\".")
    private String tokenType = "Bearer";

    @Schema(description = "The access token's lifetime in seconds.")
    private long expiresIn;

    @Schema(description = "The opaque refresh token, when the client is allowed the refresh_token grant.")
    private String refreshToken;

    @Schema(description = "The scope string as granted, e.g. \"openid profile email\".")
    private String scope;

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
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

}
