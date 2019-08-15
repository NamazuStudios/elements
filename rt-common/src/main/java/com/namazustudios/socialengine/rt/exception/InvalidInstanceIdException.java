package com.namazustudios.socialengine.rt.exception;

public class InvalidInstanceIdException extends InvalidIdException {

    public InvalidInstanceIdException() {}

    public InvalidInstanceIdException(String message) {
        super(message);
    }

    public InvalidInstanceIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInstanceIdException(Throwable cause) {
        super(cause);
    }

    public InvalidInstanceIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
