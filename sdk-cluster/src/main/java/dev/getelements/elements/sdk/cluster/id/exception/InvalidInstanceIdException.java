package dev.getelements.elements.sdk.cluster.id.exception;

/**
 * Thrown when a {@link dev.getelements.elements.sdk.cluster.id.InstanceId} cannot be parsed or constructed.
 */
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
