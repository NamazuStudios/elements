package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/** Represents a response from creating or updating an OIDC Auth Scheme for an Application. */
@Schema(description = "Represents a response from updating an Auth Scheme for an Application.")
public class CreateOrUpdateOidcAuthSchemeResponse {

    /** Creates a new instance. */
    public CreateOrUpdateOidcAuthSchemeResponse() {}

    /** The created or updated OIDC auth scheme. */
    @Schema(description = "The full JSON response as described in AuthScheme")
    public OidcAuthScheme scheme;

    /**
     * Returns the created or updated OIDC auth scheme.
     *
     * @return the scheme
     */
    public OidcAuthScheme getScheme() {
        return scheme;
    }

    /**
     * Sets the created or updated OIDC auth scheme.
     *
     * @param scheme the scheme
     */
    public void setScheme(OidcAuthScheme scheme) {
        this.scheme = scheme;
    }

}
