package com.namazustudios.socialengine.exception;

public class LeaderboardNotFoundException extends NotFoundException {

    public LeaderboardNotFoundException() {
    }

    public LeaderboardNotFoundException(String message) {
        super(message);
    }

    public LeaderboardNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LeaderboardNotFoundException(Throwable cause) {
        super(cause);
    }

    public LeaderboardNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
