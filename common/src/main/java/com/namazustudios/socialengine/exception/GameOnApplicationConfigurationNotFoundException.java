package com.namazustudios.socialengine.exception;

public class GameOnApplicationConfigurationNotFoundException extends NotFoundException {

    public GameOnApplicationConfigurationNotFoundException() {}

    public GameOnApplicationConfigurationNotFoundException(String message) {
        super(message);
    }

    public GameOnApplicationConfigurationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameOnApplicationConfigurationNotFoundException(Throwable cause) {
        super(cause);
    }

    public GameOnApplicationConfigurationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
