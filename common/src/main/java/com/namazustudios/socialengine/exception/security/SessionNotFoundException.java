package com.namazustudios.socialengine.exception.security;

import com.namazustudios.socialengine.exception.NotFoundException;

public class SessionNotFoundException extends NotFoundException {

    public SessionNotFoundException() {}

    public SessionNotFoundException(String message) {
        super(message);
    }

    public SessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionNotFoundException(Throwable cause) {
        super(cause);
    }

    public SessionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
