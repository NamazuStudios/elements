package dev.getelements.elements.sdk.model.exception.security;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;

/** Thrown when a bad session secret is provided. */
public class BadSessionSecretException extends InvalidDataException {

    /** Creates a new instance. */
    public BadSessionSecretException() {
    }

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public BadSessionSecretException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and model.
     * @param message the detail message
     * @param model the invalid model object
     */
    public BadSessionSecretException(String message, Object model) {
        super(message, model);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public BadSessionSecretException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given message, cause and model.
     * @param message the detail message
     * @param cause the cause
     * @param model the invalid model object
     */
    public BadSessionSecretException(String message, Throwable cause, Object model) {
        super(message, cause, model);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public BadSessionSecretException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance with the given cause and model.
     * @param cause the cause
     * @param model the invalid model object
     */
    public BadSessionSecretException(Throwable cause, Object model) {
        super(cause, model);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public BadSessionSecretException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
