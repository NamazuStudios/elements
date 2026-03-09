package dev.getelements.elements.sdk.model.exception;

import dev.getelements.elements.sdk.model.match.Match;

/**
 * Thrown to indicate there is no suitable {@link Match} found.
 */
public class NoSuitableMatchException extends RuntimeException {

    /** Creates a new instance. */
    public NoSuitableMatchException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public NoSuitableMatchException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public NoSuitableMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public NoSuitableMatchException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public NoSuitableMatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
