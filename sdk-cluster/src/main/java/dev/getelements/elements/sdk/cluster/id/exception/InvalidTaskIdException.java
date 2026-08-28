package dev.getelements.elements.sdk.cluster.id.exception;

/**
 * Thrown when a {@link dev.getelements.elements.sdk.cluster.id.TaskId} cannot be parsed or constructed.
 */
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
