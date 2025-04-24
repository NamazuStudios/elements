package dev.getelements.elements.sdk.jakarta.rs;

/**
 * Names the authentication schemes used by the API.
 */
public interface AuthSchemes {

    /**
     * Specifies the auth_bearer scheme used with Authorization: Bearer headers
     */
    String AUTH_BEARER = "auth_bearer";

    String SESSION_SECRET = "session_secret";

}
