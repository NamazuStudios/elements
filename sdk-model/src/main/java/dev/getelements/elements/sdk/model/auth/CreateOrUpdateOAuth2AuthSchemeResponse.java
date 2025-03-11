package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a response from updating an Auth Scheme for an Application.")
public class CreateOrUpdateOAuth2AuthSchemeResponse {

    @Schema(description = "The full JSON response as described in AuthScheme")
    public OAuth2AuthScheme scheme;

    public OAuth2AuthScheme getScheme() {
        return scheme;
    }

    public void setScheme(OAuth2AuthScheme scheme) {
        this.scheme = scheme;
    }

}
