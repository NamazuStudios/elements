package dev.getelements.elements.sdk.model.exception.crypto;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;

/** Thrown when an invalid cryptographic key is encountered. */
public class InvalidKeyException extends InvalidDataException {

    /** Creates a new instance. */
    public InvalidKeyException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public InvalidKeyException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and model.
     * @param message the detail message
     * @param model the invalid model object
     */
    public InvalidKeyException(String message, Object model) {
        super(message, model);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public InvalidKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given message, cause and model.
     * @param message the detail message
     * @param cause the cause
     * @param model the invalid model object
     */
    public InvalidKeyException(String message, Throwable cause, Object model) {
        super(message, cause, model);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public InvalidKeyException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance with the given cause and model.
     * @param cause the cause
     * @param model the invalid model object
     */
    public InvalidKeyException(Throwable cause, Object model) {
        super(cause, model);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public InvalidKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
