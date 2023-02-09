package com.namazustudios.socialengine.exception.crypto;

import com.namazustudios.socialengine.exception.InvalidDataException;

public class InvalidKeyException extends InvalidDataException {

    public InvalidKeyException() {}

    public InvalidKeyException(String message) {
        super(message);
    }

    public InvalidKeyException(String message, Object model) {
        super(message, model);
    }

    public InvalidKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidKeyException(String message, Throwable cause, Object model) {
        super(message, cause, model);
    }

    public InvalidKeyException(Throwable cause) {
        super(cause);
    }

    public InvalidKeyException(Throwable cause, Object model) {
        super(cause, model);
    }

    public InvalidKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
