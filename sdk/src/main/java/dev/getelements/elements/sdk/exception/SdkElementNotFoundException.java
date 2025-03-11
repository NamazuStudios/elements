package dev.getelements.elements.sdk.exception;

import dev.getelements.elements.sdk.ElementLoaderFactory;

/**
 * Thrown when a {@link ElementLoaderFactory} cannot find the requested element.
 */
public class SdkElementNotFoundException extends SdkException {
    public SdkElementNotFoundException() {}

    public SdkElementNotFoundException(String message) {
        super(message);
    }

    public SdkElementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SdkElementNotFoundException(Throwable cause) {
        super(cause);
    }

    public SdkElementNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
