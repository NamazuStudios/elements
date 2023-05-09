package dev.getelements.elements.exception.item;

import dev.getelements.elements.exception.NotFoundException;

public class ItemNotFoundException extends NotFoundException {

    public ItemNotFoundException() {}

    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemNotFoundException(Throwable cause) {
        super(cause);
    }

    public ItemNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
