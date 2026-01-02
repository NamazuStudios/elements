package dev.getelements.elements.sdk.model.exception.auth;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;

public class AuthValidationException extends ForbiddenException {
    public AuthValidationException(String message) {
        super(message);
    }
}
