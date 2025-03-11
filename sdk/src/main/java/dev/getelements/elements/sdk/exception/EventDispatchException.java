package dev.getelements.elements.sdk.exception;

public class EventDispatchException extends SdkException {

    public EventDispatchException() {}

    public EventDispatchException(String message) {
        super(message);
    }

    public EventDispatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventDispatchException(Throwable cause) {
        super(cause);
    }

    public EventDispatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
