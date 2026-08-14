package dev.getelements.elements.sdk.model.exception.profile;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;

/**
 * Thrown when creating a profile would push a user's profile count for an application over the application's
 * configured {@code maxProfiles}.
 */
public class ProfileLimitExceededException extends InvalidDataException {

    /** Creates a new instance. */
    public ProfileLimitExceededException() {
    }

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public ProfileLimitExceededException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public ProfileLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public ProfileLimitExceededException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public ProfileLimitExceededException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
