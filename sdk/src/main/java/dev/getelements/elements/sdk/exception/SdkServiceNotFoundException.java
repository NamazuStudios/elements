package dev.getelements.elements.sdk.exception;

public class SdkServiceNotFoundException extends SdkException {

    public SdkServiceNotFoundException() {}

    public SdkServiceNotFoundException(String message) {
        super(message);
    }

    public SdkServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SdkServiceNotFoundException(Throwable cause) {
        super(cause);
    }

    public SdkServiceNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
