package dev.getelements.elements.sdk.cluster.id.exception;

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
