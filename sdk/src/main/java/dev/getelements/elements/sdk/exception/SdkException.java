package dev.getelements.elements.sdk.exception;

import dev.getelements.elements.sdk.ElementLoader;

/**
 * Represents a generic exception for the {@link ElementLoader} and related types.
 */
public class SdkException extends RuntimeException {

    public SdkException() {}

    public SdkException(String message) {
        super(message);
    }

    public SdkException(String message, Throwable cause) {
        super(message, cause);
    }

    public SdkException(Throwable cause) {
        super(cause);
    }

    public SdkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
