package dev.getelements.elements.sdk.model.exception;

/** Thrown when an invalid combination of parameters is provided. */
public class BadParameterCombinationException extends InvalidDataException {

    /** Creates a new instance. */
    public BadParameterCombinationException() {
    }

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public BadParameterCombinationException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public BadParameterCombinationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public BadParameterCombinationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public BadParameterCombinationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
