package dev.getelements.elements.sdk.model.oidc;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/**
 * The provider's {@code /.well-known/openid-configuration} discovery document. The {@code issuer} value is
 * the base URL all other endpoints are resolved against; per the OIDC discovery specification the document
 * must be served at {@code <issuer>/.well-known/openid-configuration}.
 */
@Schema(description = "The OIDC provider discovery document.")
public class OidcProviderDiscoveryDocument {

    @Schema(description = "The base issuer URL. Also the value of the iss claim in issued id_tokens.")
    private String issuer;

    @Schema(description = "The authorization endpoint URL.")
    private String authorizationEndpoint;

    @Schema(description = "The token endpoint URL.")
    private String tokenEndpoint;

    @Schema(description = "The userinfo endpoint URL.")
    private String userinfoEndpoint;

    @Schema(description = "The end-session (logout) endpoint URL.")
    private String endSessionEndpoint;

    @Schema(description = "The JWKS URL publishing this deployment's public signing keys.")
    private String jwksUri;

    @Schema(description = "The grant types the provider supports.")
    private Set<String> grantTypesSupported;

    @Schema(description = "The scopes the provider supports.")
    private Set<String> scopesSupported;

    @Schema(description = "The id_token signing algorithms the provider supports.")
    private Set<String> idTokenSigningAlgValuesSupported;

    @Schema(description = "The response types the provider supports.")
    private Set<String> responseTypesSupported;

    @Schema(description = "The PKCE code challenge methods the provider supports.")
    private Set<String> codeChallengeMethodsSupported;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getUserinfoEndpoint() {
        return userinfoEndpoint;
    }

    public void setUserinfoEndpoint(String userinfoEndpoint) {
        this.userinfoEndpoint = userinfoEndpoint;
    }

    public String getEndSessionEndpoint() {
        return endSessionEndpoint;
    }

    public void setEndSessionEndpoint(String endSessionEndpoint) {
        this.endSessionEndpoint = endSessionEndpoint;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public Set<String> getGrantTypesSupported() {
        return grantTypesSupported;
    }

    public void setGrantTypesSupported(Set<String> grantTypesSupported) {
        this.grantTypesSupported = grantTypesSupported;
    }

    public Set<String> getScopesSupported() {
        return scopesSupported;
    }

    public void setScopesSupported(Set<String> scopesSupported) {
        this.scopesSupported = scopesSupported;
    }

    public Set<String> getIdTokenSigningAlgValuesSupported() {
        return idTokenSigningAlgValuesSupported;
    }

    public void setIdTokenSigningAlgValuesSupported(Set<String> idTokenSigningAlgValuesSupported) {
        this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
    }

    public Set<String> getResponseTypesSupported() {
        return responseTypesSupported;
    }

    public void setResponseTypesSupported(Set<String> responseTypesSupported) {
        this.responseTypesSupported = responseTypesSupported;
    }

    public Set<String> getCodeChallengeMethodsSupported() {
        return codeChallengeMethodsSupported;
    }

    public void setCodeChallengeMethodsSupported(Set<String> codeChallengeMethodsSupported) {
        this.codeChallengeMethodsSupported = codeChallengeMethodsSupported;
    }

}
