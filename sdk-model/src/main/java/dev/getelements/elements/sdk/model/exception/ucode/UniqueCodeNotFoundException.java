package dev.getelements.elements.sdk.model.exception.ucode;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class UniqueCodeNotFoundException extends NotFoundException {

    public UniqueCodeNotFoundException() {}

    public UniqueCodeNotFoundException(String message) {
        super(message);
    }

    public UniqueCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniqueCodeNotFoundException(Throwable cause) {
        super(cause);
    }

    public UniqueCodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
