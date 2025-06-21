package dev.getelements.elements.sdk.model.exception;

public class MultiMatchNotFoundException extends NotFoundException{

    public MultiMatchNotFoundException() {}

    public MultiMatchNotFoundException(String message) {
        super(message);
    }

    public MultiMatchNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultiMatchNotFoundException(Throwable cause) {
        super(cause);
    }

    public MultiMatchNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
