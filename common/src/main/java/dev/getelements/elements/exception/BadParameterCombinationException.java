package dev.getelements.elements.exception;

public class BadParameterCombinationException extends InvalidDataException {

    public BadParameterCombinationException() {
    }

    public BadParameterCombinationException(String message) {
        super(message);
    }

    public BadParameterCombinationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadParameterCombinationException(Throwable cause) {
        super(cause);
    }

    public BadParameterCombinationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
