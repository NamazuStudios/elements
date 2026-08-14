package dev.getelements.elements.service.auth.oidc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The subset of an OIDC provider's discovery document (normally served at
 * {@code /.well-known/openid-configuration}) needed to drive the authorization code flow. Internal wire-format
 * DTO only — field names follow the snake_case wire format defined by the OIDC Discovery specification. Not part
 * of the public SDK model since {@code sdk-model} has no Jackson dependency.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OidcDiscoveryDocument {

    @JsonProperty("issuer")
    private String issuer;

    @JsonProperty("authorization_endpoint")
    private String authorizationEndpoint;

    @JsonProperty("token_endpoint")
    private String tokenEndpoint;

    @JsonProperty("jwks_uri")
    private String jwksUri;

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

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

}
