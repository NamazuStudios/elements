package dev.getelements.elements.sdk.model.exception.inventory;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

/** Thrown when a distinct inventory item cannot be found. */
public class DistinctInventoryItemNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public DistinctInventoryItemNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public DistinctInventoryItemNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public DistinctInventoryItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public DistinctInventoryItemNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public DistinctInventoryItemNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
