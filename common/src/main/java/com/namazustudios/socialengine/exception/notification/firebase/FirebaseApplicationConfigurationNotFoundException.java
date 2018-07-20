package com.namazustudios.socialengine.exception.notification.firebase;

import com.namazustudios.socialengine.exception.NotFoundException;

public class FirebaseApplicationConfigurationNotFoundException extends NotFoundException {
    public FirebaseApplicationConfigurationNotFoundException() {}

    public FirebaseApplicationConfigurationNotFoundException(String message) {
        super(message);
    }

    public FirebaseApplicationConfigurationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FirebaseApplicationConfigurationNotFoundException(Throwable cause) {
        super(cause);
    }

    public FirebaseApplicationConfigurationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
