package com.namazustudios.socialengine.exception.gameon;

import com.namazustudios.socialengine.exception.NotFoundException;

public class GameOnSessionNotFoundException extends NotFoundException {

    public GameOnSessionNotFoundException() {}

    public GameOnSessionNotFoundException(String message) {
        super(message);
    }

    public GameOnSessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameOnSessionNotFoundException(Throwable cause) {
        super(cause);
    }

    public GameOnSessionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
