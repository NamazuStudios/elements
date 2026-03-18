package dev.getelements.elements.sdk.model.exception.largeobject;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

/** Thrown when the content of a large object cannot be found. */
public class LargeObjectContentNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public LargeObjectContentNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public LargeObjectContentNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public LargeObjectContentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public LargeObjectContentNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public LargeObjectContentNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
