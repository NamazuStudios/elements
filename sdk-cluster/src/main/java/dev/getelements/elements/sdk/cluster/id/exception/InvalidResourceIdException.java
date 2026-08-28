package dev.getelements.elements.sdk.cluster.id.exception;

/**
 * Thrown when a {@link dev.getelements.elements.sdk.cluster.id.ResourceId} cannot be parsed or constructed.
 */
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
