package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/** How the client authenticates to an OIDC provider's token endpoint during code exchange. */
@Schema(description = "How the client authenticates to the token endpoint during authorization code exchange.")
public enum TokenEndpointAuthMethod {

    /** Sends the client id and secret as an HTTP Basic Authorization header. */
    CLIENT_SECRET_BASIC,

    /** Sends the client id and secret as additional form parameters in the request body. */
    CLIENT_SECRET_POST

}
