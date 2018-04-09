package com.namazustudios.socialengine.exception;

public class FriendNotFoundException extends NotFoundException {

    public FriendNotFoundException() {}

    public FriendNotFoundException(String message) {
        super(message);
    }

    public FriendNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FriendNotFoundException(Throwable cause) {
        super(cause);
    }

    public FriendNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
