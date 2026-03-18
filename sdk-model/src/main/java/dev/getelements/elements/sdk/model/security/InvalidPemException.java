package dev.getelements.elements.sdk.model.security;

/** Exception thrown when PEM data is malformed or cannot be parsed. */
public class InvalidPemException extends Exception {

    /** Creates a new instance with no message. */
    public InvalidPemException() {}

    /**
     * Creates a new instance with the given message.
     *
     * @param message the detail message
     */
    public InvalidPemException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public InvalidPemException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     *
     * @param cause the cause
     */
    public InvalidPemException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance with full control over suppression and stack trace.
     *
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public InvalidPemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
