package dev.getelements.elements.sdk.cluster.id.exception;

public class InvalidInstanceIdException extends InvalidIdException {

    public InvalidInstanceIdException() {}

    public InvalidInstanceIdException(String message) {
        super(message);
    }

    public InvalidInstanceIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInstanceIdException(Throwable cause) {
        super(cause);
    }

}
