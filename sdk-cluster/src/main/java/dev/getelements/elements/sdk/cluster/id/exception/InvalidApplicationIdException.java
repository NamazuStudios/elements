package dev.getelements.elements.sdk.cluster.id.exception;

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

}
