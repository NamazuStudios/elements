package com.namazustudios.socialengine.exception.gameon;

import com.namazustudios.socialengine.exception.NotFoundException;

public class GameOnMatchNotFoundException extends NotFoundException {

    public GameOnMatchNotFoundException() {}

    public GameOnMatchNotFoundException(String message) {
        super(message);
    }

    public GameOnMatchNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameOnMatchNotFoundException(Throwable cause) {
        super(cause);
    }

    public GameOnMatchNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
