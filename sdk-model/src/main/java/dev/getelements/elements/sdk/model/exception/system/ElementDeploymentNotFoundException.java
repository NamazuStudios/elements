package dev.getelements.elements.sdk.model.exception.system;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class ElementDeploymentNotFoundException extends NotFoundException {

    public ElementDeploymentNotFoundException() {}

    public ElementDeploymentNotFoundException(String message) {
        super(message);
    }

    public ElementDeploymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElementDeploymentNotFoundException(Throwable cause) {
        super(cause);
    }

    public ElementDeploymentNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
