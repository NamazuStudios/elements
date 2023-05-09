package dev.getelements.elements.exception.profile;

import dev.getelements.elements.exception.NotFoundException;

public class ProfileNotFoundException extends NotFoundException {

    public ProfileNotFoundException() {
    }

    public ProfileNotFoundException(String message) {
        super(message);
    }

    public ProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfileNotFoundException(Throwable cause) {
        super(cause);
    }

    public ProfileNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
