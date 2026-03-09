package dev.getelements.elements.sdk.model.exception;

/** Thrown when a multi-match cannot be found. */
public class MultiMatchNotFoundException extends NotFoundException{

    /** Creates a new instance. */
    public MultiMatchNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public MultiMatchNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public MultiMatchNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public MultiMatchNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public MultiMatchNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
