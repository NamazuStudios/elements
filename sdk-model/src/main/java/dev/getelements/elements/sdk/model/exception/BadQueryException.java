package dev.getelements.elements.sdk.model.exception;

/**
 * Thrown when a bad search query is encountered.
 */
public class BadQueryException extends InvalidDataException {

    /** Creates a new instance. */
    public BadQueryException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public BadQueryException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and model.
     * @param message the detail message
     * @param model the invalid model object
     */
    public BadQueryException(String message, Object model) {
        super(message, model);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public BadQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given message, cause and model.
     * @param message the detail message
     * @param cause the cause
     * @param model the invalid model object
     */
    public BadQueryException(String message, Throwable cause, Object model) {
        super(message, cause, model);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public BadQueryException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance with the given cause and model.
     * @param cause the cause
     * @param model the invalid model object
     */
    public BadQueryException(Throwable cause, Object model) {
        super(cause, model);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public BadQueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
