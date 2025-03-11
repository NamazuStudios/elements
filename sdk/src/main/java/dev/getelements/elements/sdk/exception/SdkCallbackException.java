package dev.getelements.elements.sdk.exception;

public class SdkCallbackException extends SdkException {

    public SdkCallbackException() {
    }

    public SdkCallbackException(String message) {
        super(message);
    }

    public SdkCallbackException(String message, Throwable cause) {
        super(message, cause);
    }

    public SdkCallbackException(Throwable cause) {
        super(cause);
    }

    public SdkCallbackException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
