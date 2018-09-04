package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.service.gameon.client.exception.PlayerSessionExpiredException;

import java.util.function.Function;
import java.util.function.Supplier;

public interface PlayerRequestBuilder<BuiltT> {

    /**
     * Specifies the {@link GameOnSession} to use when making the request.
     *
     * @param gameOnSession the {@link GameOnSession}
     * @return this instance
     */
    PlayerRequestBuilder<BuiltT> withSession(GameOnSession gameOnSession);

    /**
     * Indicates that the {@link PlayerRequestBuilder<BuiltT>} is to attempt refresh a session with GameOn should it
     * expire.
     *
     * @param unauthorizedHandler
     * @return
     */
    PlayerRequestBuilder<BuiltT> withExpirationRetry(Function<PlayerSessionExpiredException, GameOnSession> unauthorizedHandler);

    /**
     * Builds the instance, checking for sanity first.
     *
     * @return the built instance
     */
    BuiltT build();

}
