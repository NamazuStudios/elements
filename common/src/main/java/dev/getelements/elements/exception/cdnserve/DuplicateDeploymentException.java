package dev.getelements.elements.exception.cdnserve;

import dev.getelements.elements.exception.DuplicateException;

public class DuplicateDeploymentException extends DuplicateException {

    public DuplicateDeploymentException() {}

    public DuplicateDeploymentException(String message) {
        super(message);
    }

    public DuplicateDeploymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateDeploymentException(Throwable cause) {
        super(cause);
    }

    public DuplicateDeploymentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
