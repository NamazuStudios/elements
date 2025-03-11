package dev.getelements.elements.sdk.model.exception.security;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;

public class InsufficientPermissionException extends ForbiddenException {

    public InsufficientPermissionException() {}

    public InsufficientPermissionException(String message) {
        super(message);
    }

    public InsufficientPermissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientPermissionException(Throwable cause) {
        super(cause);
    }

    public InsufficientPermissionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
