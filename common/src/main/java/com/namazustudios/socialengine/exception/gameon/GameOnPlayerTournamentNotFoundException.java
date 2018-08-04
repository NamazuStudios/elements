package com.namazustudios.socialengine.exception.gameon;

import com.namazustudios.socialengine.exception.NotFoundException;

public class GameOnPlayerTournamentNotFoundException extends NotFoundException {

    public GameOnPlayerTournamentNotFoundException() {}

    public GameOnPlayerTournamentNotFoundException(String message) {
        super(message);
    }

    public GameOnPlayerTournamentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameOnPlayerTournamentNotFoundException(Throwable cause) {
        super(cause);
    }

    public GameOnPlayerTournamentNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
