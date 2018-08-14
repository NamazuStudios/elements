package com.namazustudios.socialengine.service.gameon.client.invoker.builder;

import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnMatchInvoker;
import com.namazustudios.socialengine.service.gameon.client.invoker.v1.V1GameOnMatchInvoker;

import javax.ws.rs.client.Client;

public class DefaultGameOnMatchInvokerBuilder extends AbstractPlayerRequestBuilder<GameOnMatchInvoker>
                                              implements GameOnMatchInvoker.Builder {

    @Override
    protected GameOnMatchInvoker doBuild(final Client client, final GameOnSession gameOnSession) {
        return new V1GameOnMatchInvoker(client, gameOnSession);
    }

}
