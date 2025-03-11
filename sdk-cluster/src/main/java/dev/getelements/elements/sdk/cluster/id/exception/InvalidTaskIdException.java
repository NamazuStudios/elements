package dev.getelements.elements.sdk.cluster.id.exception;

public class InvalidTaskIdException extends InvalidIdException {

    public InvalidTaskIdException() {}

    public InvalidTaskIdException(String message) {
        super(message);
    }

    public InvalidTaskIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTaskIdException(Throwable cause) {
        super(cause);
    }

}
