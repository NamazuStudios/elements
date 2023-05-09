package dev.getelements.elements.rt.exception;

public class InvalidResourceIdException extends InvalidIdException {

    public InvalidResourceIdException() {}

    public InvalidResourceIdException(String message) {
        super(message);
    }

    public InvalidResourceIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidResourceIdException(Throwable cause) {
        super(cause);
    }

    public InvalidResourceIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
