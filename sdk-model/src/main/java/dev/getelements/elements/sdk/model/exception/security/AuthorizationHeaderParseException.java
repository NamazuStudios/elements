package dev.getelements.elements.sdk.model.exception.security;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;

/**
 * Thrown when the authorization header cannot be parsed.
 */
public class AuthorizationHeaderParseException extends ForbiddenException {

    /** Creates a new instance. */
    public AuthorizationHeaderParseException() {}

    /**
     * Creates a new instance with the given message.
     * @param s the detail message
     */
    public AuthorizationHeaderParseException(String s) {
        super(s);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public AuthorizationHeaderParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public AuthorizationHeaderParseException(Throwable cause) {
        super(cause);
    }

}
