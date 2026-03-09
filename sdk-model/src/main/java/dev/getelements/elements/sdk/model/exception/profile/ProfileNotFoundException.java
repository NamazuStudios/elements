package dev.getelements.elements.sdk.model.exception.profile;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

/** Thrown when a profile cannot be found. */
public class ProfileNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public ProfileNotFoundException() {
    }

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public ProfileNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public ProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public ProfileNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public ProfileNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
