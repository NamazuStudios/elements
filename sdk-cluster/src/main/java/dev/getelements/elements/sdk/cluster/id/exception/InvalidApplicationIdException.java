package dev.getelements.elements.sdk.cluster.id.exception;

/**
 * Thrown when a {@link dev.getelements.elements.sdk.cluster.id.ApplicationId} cannot be parsed or constructed.
 */
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
