package dev.getelements.elements.sdk.transact.exception;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class NullResourceException extends NotFoundException {

    public NullResourceException() {}

    public NullResourceException(String message) {
        super(message);
    }

    public NullResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NullResourceException(Throwable cause) {
        super(cause);
    }

    public NullResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
