package dev.getelements.elements.sdk.cluster.id.exception;

/**
 * Thrown when a {@link dev.getelements.elements.sdk.cluster.id.DeploymentId} cannot be parsed or constructed.
 */
public class InvalidDeploymentIdException extends InvalidIdException {

    public InvalidDeploymentIdException() {}

    public InvalidDeploymentIdException(String message) {
        super(message);
    }

    public InvalidDeploymentIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDeploymentIdException(Throwable cause) {
        super(cause);
    }

}
