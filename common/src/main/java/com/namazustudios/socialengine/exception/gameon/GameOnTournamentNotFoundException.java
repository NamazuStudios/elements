package com.namazustudios.socialengine.exception.gameon;

import com.namazustudios.socialengine.exception.NotFoundException;

public class GameOnTournamentNotFoundException extends NotFoundException {

    public GameOnTournamentNotFoundException() {}

    public GameOnTournamentNotFoundException(String message) {
        super(message);
    }

    public GameOnTournamentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameOnTournamentNotFoundException(Throwable cause) {
        super(cause);
    }

    public GameOnTournamentNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
