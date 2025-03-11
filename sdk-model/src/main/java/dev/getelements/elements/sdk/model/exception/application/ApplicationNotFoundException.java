package dev.getelements.elements.sdk.model.exception.application;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class ApplicationNotFoundException extends NotFoundException {

    public ApplicationNotFoundException() {}

    public ApplicationNotFoundException(String message) {
        super(message);
    }

    public ApplicationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationNotFoundException(Throwable cause) {
        super(cause);
    }

    public ApplicationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
