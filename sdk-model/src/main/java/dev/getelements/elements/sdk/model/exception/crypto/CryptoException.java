package dev.getelements.elements.sdk.model.exception.crypto;

import dev.getelements.elements.sdk.model.exception.InternalException;

/** Thrown when a cryptographic operation fails. */
public class CryptoException extends InternalException {

    /** Creates a new instance. */
    public CryptoException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public CryptoException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public CryptoException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public CryptoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
