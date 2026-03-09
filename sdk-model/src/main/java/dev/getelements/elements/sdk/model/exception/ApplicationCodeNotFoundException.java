package dev.getelements.elements.sdk.model.exception;

/** Thrown when an application code cannot be found. */
public class ApplicationCodeNotFoundException extends InternalException {

    /** Creates a new instance. */
    public ApplicationCodeNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public ApplicationCodeNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public ApplicationCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public ApplicationCodeNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public ApplicationCodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
