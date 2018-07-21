package com.namazustudios.socialengine.exception.gameon;

import com.namazustudios.socialengine.exception.NotFoundException;

public class GameOnRegistrationNotFoundException extends NotFoundException {

    public GameOnRegistrationNotFoundException() {}

    public GameOnRegistrationNotFoundException(String message) {
        super(message);
    }

    public GameOnRegistrationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameOnRegistrationNotFoundException(Throwable cause) {
        super(cause);
    }

    public GameOnRegistrationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
