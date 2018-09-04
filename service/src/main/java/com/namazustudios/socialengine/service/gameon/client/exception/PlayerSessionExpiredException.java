package com.namazustudios.socialengine.service.gameon.client.exception;

import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.service.gameon.client.model.ErrorResponse;

import javax.ws.rs.core.Response;

/**
 * Used to indicate that the Amazon GameOn session has expired.  Typically indicated with a response status of
 * 401.
 */
public class PlayerSessionExpiredException extends RuntimeException {

    private final GameOnSession expired;

    private final ErrorResponse errorResponse;

    public PlayerSessionExpiredException(final GameOnSession expired, final ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.expired = expired;
        this.errorResponse = errorResponse;
    }

    public GameOnSession getExpired() {
        return expired;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

}
