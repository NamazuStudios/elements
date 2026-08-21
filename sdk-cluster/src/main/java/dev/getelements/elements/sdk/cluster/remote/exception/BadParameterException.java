package dev.getelements.elements.sdk.cluster.remote.exception;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;

public class BadParameterException extends InvalidDataException {

    public BadParameterException() {}

    public BadParameterException(String message) {
        super(message);
    }

    public BadParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadParameterException(Throwable cause) {
        super(cause);
    }

    public BadParameterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
