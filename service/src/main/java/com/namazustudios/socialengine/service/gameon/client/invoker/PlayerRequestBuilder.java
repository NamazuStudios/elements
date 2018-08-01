package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.GameOnSession;

public interface PlayerRequestBuilder<BuiltT> {

    /**
     * Specifies the {@link GameOnSession} to use when making the request.
     *
     * @param gameOnSession the {@link GameOnSession}
     * @return this instance
     */
    PlayerRequestBuilder<BuiltT> withSession(GameOnSession gameOnSession);

    /**
     * Builds the instance, checking for sanity first.
     *
     * @return the built instance
     */
    BuiltT build();

}
