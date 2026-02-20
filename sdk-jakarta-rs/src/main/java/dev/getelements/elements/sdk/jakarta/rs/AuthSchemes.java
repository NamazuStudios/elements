package dev.getelements.elements.sdk.jakarta.rs;

/**
 * Names the authentication schemes used by the API.
 */
public interface AuthSchemes {

    /**
     * Specifies the auth_bearer scheme used with Authorization: Bearer headers
     */
    String AUTH_BEARER = "auth_bearer";

    /**
     * Specifies the session_secret scheme used with the Elements-SessionSecret: headers.
     */
    String SESSION_SECRET = "session_secret";

}
