package com.namazustudios.socialengine.service.gameon.client.invoker.builder;

import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnGamePrizeInvoker;
import com.namazustudios.socialengine.service.gameon.client.invoker.v1.V1StandardSecurityGamePrizeInvoker;

import javax.ws.rs.client.Client;

public class DefaultGameOnGamePrizeInvokerBuilder extends AbstractPlayerRequestBuilder<GameOnGamePrizeInvoker>
                                                  implements GameOnGamePrizeInvoker.Builder {

    public DefaultGameOnGamePrizeInvokerBuilder() {
        super(GameOnGamePrizeInvoker.class);
    }

    @Override
    protected GameOnGamePrizeInvoker doBuild(final Client client, final GameOnSession gameOnSession) {
        return new V1StandardSecurityGamePrizeInvoker(client, gameOnSession);
    }

}
