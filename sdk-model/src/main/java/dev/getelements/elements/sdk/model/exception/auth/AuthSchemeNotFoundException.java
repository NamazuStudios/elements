package dev.getelements.elements.sdk.model.exception.auth;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class AuthSchemeNotFoundException extends NotFoundException {

    public AuthSchemeNotFoundException() {}

    public AuthSchemeNotFoundException(String message) {
        super(message);
    }

    public AuthSchemeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthSchemeNotFoundException(Throwable cause) {
        super(cause);
    }

    public AuthSchemeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
