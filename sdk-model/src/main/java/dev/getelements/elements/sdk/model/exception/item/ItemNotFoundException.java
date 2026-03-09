package dev.getelements.elements.sdk.model.exception.item;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

/** Thrown when an item cannot be found. */
public class ItemNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public ItemNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public ItemNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public ItemNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public ItemNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
