package dev.getelements.elements.sdk.model.exception.application;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class FirebaseApplicationConfigurationNotFoundException extends NotFoundException {

    public FirebaseApplicationConfigurationNotFoundException() {}

    public FirebaseApplicationConfigurationNotFoundException(final String message) {
        super(message);
    }

    public FirebaseApplicationConfigurationNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FirebaseApplicationConfigurationNotFoundException(final Throwable cause) {
        super(cause);
    }

    public FirebaseApplicationConfigurationNotFoundException(final String message,
                                                             final Throwable cause,
                                                             final boolean enableSuppression,
                                                             final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
