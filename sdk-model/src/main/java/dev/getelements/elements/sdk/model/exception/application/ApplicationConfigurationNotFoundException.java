package dev.getelements.elements.sdk.model.exception.application;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class ApplicationConfigurationNotFoundException extends NotFoundException {

    public ApplicationConfigurationNotFoundException() {}

    public ApplicationConfigurationNotFoundException(String message) {
        super(message);
    }

    public ApplicationConfigurationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationConfigurationNotFoundException(Throwable cause) {
        super(cause);
    }

    public ApplicationConfigurationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
