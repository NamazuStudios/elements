package dev.getelements.elements.sdk.cluster.remote.exception;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class ParameterNotFoundException extends NotFoundException {

    public ParameterNotFoundException() {
        super();
    }

    public ParameterNotFoundException(String message) {
        super(message);
    }

    public ParameterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParameterNotFoundException(Throwable cause) {
        super(cause);
    }

    public ParameterNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
