package com.namazustudios.socialengine.rt.exception;

public class InvalidTaskIdException extends InvalidIdException {

    public InvalidTaskIdException() {}

    public InvalidTaskIdException(String message) {
        super(message);
    }

    public InvalidTaskIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTaskIdException(Throwable cause) {
        super(cause);
    }

    public InvalidTaskIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
