package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a response from updating an Auth Scheme for an Application.")
public class CreateOrUpdateOidcAuthSchemeResponse {

    @Schema(description = "The full JSON response as described in AuthScheme")
    public OidcAuthScheme scheme;

    public OidcAuthScheme getScheme() {
        return scheme;
    }

    public void setScheme(OidcAuthScheme scheme) {
        this.scheme = scheme;
    }

}
