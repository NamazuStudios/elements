package dev.getelements.elements.sdk.model.exception.auth;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;

/** Thrown when authentication validation fails. */
public class AuthValidationException extends ForbiddenException {

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public AuthValidationException(String message) {
        super(message);
    }
}
