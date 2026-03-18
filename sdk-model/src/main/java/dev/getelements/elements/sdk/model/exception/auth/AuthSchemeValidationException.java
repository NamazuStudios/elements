package dev.getelements.elements.sdk.model.exception.auth;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;

/** Thrown when an auth scheme fails validation. */
public class AuthSchemeValidationException extends InvalidDataException {

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public AuthSchemeValidationException(String message) {
        super(message);
    }
}
