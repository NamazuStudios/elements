package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/** Represents a response from creating or updating an OAuth2 Auth Scheme for an Application. */
@Schema(description = "Represents a response from updating an Auth Scheme for an Application.")
public class CreateOrUpdateOAuth2AuthSchemeResponse {

    /** Creates a new instance. */
    public CreateOrUpdateOAuth2AuthSchemeResponse() {}

    /** The created or updated OAuth2 auth scheme. */
    @Schema(description = "The full JSON response as described in AuthScheme")
    public OAuth2AuthScheme scheme;

    /**
     * Returns the created or updated OAuth2 auth scheme.
     *
     * @return the scheme
     */
    public OAuth2AuthScheme getScheme() {
        return scheme;
    }

    /**
     * Sets the created or updated OAuth2 auth scheme.
     *
     * @param scheme the scheme
     */
    public void setScheme(OAuth2AuthScheme scheme) {
        this.scheme = scheme;
    }

}
