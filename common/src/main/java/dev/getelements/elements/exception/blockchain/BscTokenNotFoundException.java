package dev.getelements.elements.exception.blockchain;

import dev.getelements.elements.exception.NotFoundException;

public class BscTokenNotFoundException extends NotFoundException {
    public BscTokenNotFoundException() {}

    public BscTokenNotFoundException(String message) {
        super(message);
    }

    public BscTokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BscTokenNotFoundException(Throwable cause) {
        super(cause);
    }

    public BscTokenNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
