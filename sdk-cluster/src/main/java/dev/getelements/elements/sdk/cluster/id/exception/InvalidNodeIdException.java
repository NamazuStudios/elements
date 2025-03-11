package dev.getelements.elements.sdk.cluster.id.exception;

public class InvalidNodeIdException extends InvalidIdException {

    public InvalidNodeIdException() {}

    public InvalidNodeIdException(String message) {
        super(message);
    }

    public InvalidNodeIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidNodeIdException(Throwable cause) {
        super(cause);
    }

}
