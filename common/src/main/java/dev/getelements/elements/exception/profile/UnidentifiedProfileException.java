package dev.getelements.elements.exception.profile;

import dev.getelements.elements.exception.NotFoundException;

public class UnidentifiedProfileException extends NotFoundException {

    public UnidentifiedProfileException() {}

    public UnidentifiedProfileException(String message) {
        super(message);
    }

    public UnidentifiedProfileException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnidentifiedProfileException(Throwable cause) {
        super(cause);
    }

    public UnidentifiedProfileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
