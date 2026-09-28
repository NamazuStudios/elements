package dev.getelements.elements.sdk.model.exception.auth;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

/** Thrown when no CAPTCHA configuration has been set up yet. */
public class CaptchaConfigurationNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public CaptchaConfigurationNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public CaptchaConfigurationNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public CaptchaConfigurationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public CaptchaConfigurationNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public CaptchaConfigurationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
