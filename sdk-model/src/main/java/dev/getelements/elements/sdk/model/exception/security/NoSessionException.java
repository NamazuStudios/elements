package dev.getelements.elements.sdk.model.exception.security;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;

/** Thrown when no valid session exists for a request. */
public class NoSessionException extends ForbiddenException {

    /** Creates a new instance. */
    public NoSessionException() {
    }

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public NoSessionException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public NoSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public NoSessionException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public NoSessionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
