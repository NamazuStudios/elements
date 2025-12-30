package dev.getelements.elements.sdk.model.exception.auth;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;

public class AuthSchemeValidationException extends InvalidDataException {
    public AuthSchemeValidationException(String message) {
        super(message);
    }
}
