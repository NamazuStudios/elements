package dev.getelements.elements.sdk.model.exception.application;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

/** Thrown when a Firebase application configuration cannot be found. */
public class FirebaseApplicationConfigurationNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public FirebaseApplicationConfigurationNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public FirebaseApplicationConfigurationNotFoundException(final String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public FirebaseApplicationConfigurationNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public FirebaseApplicationConfigurationNotFoundException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public FirebaseApplicationConfigurationNotFoundException(final String message,
                                                             final Throwable cause,
                                                             final boolean enableSuppression,
                                                             final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
