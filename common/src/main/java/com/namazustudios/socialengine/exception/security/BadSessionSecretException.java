package com.namazustudios.socialengine.exception.security;

import com.namazustudios.socialengine.exception.InvalidDataException;

public class BadSessionSecretException extends InvalidDataException {

    public BadSessionSecretException() {
    }

    public BadSessionSecretException(String message) {
        super(message);
    }

    public BadSessionSecretException(String message, Object model) {
        super(message, model);
    }

    public BadSessionSecretException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadSessionSecretException(String message, Throwable cause, Object model) {
        super(message, cause, model);
    }

    public BadSessionSecretException(Throwable cause) {
        super(cause);
    }

    public BadSessionSecretException(Throwable cause, Object model) {
        super(cause, model);
    }

    public BadSessionSecretException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
