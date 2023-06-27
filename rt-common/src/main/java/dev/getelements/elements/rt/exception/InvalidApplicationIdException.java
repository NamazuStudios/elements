package dev.getelements.elements.rt.exception;

public class InvalidApplicationIdException extends InvalidIdException {
    public InvalidApplicationIdException() {}

    public InvalidApplicationIdException(String message) {
        super(message);
    }

    public InvalidApplicationIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidApplicationIdException(Throwable cause) {
        super(cause);
    }

    public InvalidApplicationIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
