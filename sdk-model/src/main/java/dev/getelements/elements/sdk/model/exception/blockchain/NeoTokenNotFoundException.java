package dev.getelements.elements.sdk.model.exception.blockchain;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class NeoTokenNotFoundException extends NotFoundException {
    public NeoTokenNotFoundException() {}

    public NeoTokenNotFoundException(String message) {
        super(message);
    }

    public NeoTokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeoTokenNotFoundException(Throwable cause) {
        super(cause);
    }

    public NeoTokenNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
