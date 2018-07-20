package com.namazustudios.socialengine.exception.gameon;

import com.namazustudios.socialengine.exception.InternalException;

public class GameOnConfigurationException extends InternalException {

    public GameOnConfigurationException() {}

    public GameOnConfigurationException(String message) {
        super(message);
    }

    public GameOnConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameOnConfigurationException(Throwable cause) {
        super(cause);
    }

    public GameOnConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
