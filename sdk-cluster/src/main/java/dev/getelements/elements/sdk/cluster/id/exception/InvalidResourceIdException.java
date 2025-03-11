package dev.getelements.elements.sdk.cluster.id.exception;

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

}
