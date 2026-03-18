package dev.getelements.elements.sdk.model.exception.ucode;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

/** Thrown when a unique code cannot be found. */
public class UniqueCodeNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public UniqueCodeNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public UniqueCodeNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public UniqueCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public UniqueCodeNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public UniqueCodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
