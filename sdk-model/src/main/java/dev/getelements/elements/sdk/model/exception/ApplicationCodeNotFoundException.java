package dev.getelements.elements.sdk.model.exception;

public class ApplicationCodeNotFoundException extends InternalException {

    public ApplicationCodeNotFoundException() {}

    public ApplicationCodeNotFoundException(String message) {
        super(message);
    }

    public ApplicationCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationCodeNotFoundException(Throwable cause) {
        super(cause);
    }

    public ApplicationCodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
