package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.GameOnSession;
import com.namazustudios.socialengine.service.gameon.client.invoker.v1.V1GameOnPlayerTournamentInvoker;

import javax.ws.rs.client.Client;

public class DefaultGameOnPlayerTournamentInvokerBuilder
        extends AbstractPlayerRequestBuilder<GameOnPlayerTournamentInvoker>
        implements GameOnPlayerTournamentInvoker.Builder {

    @Override
    protected GameOnPlayerTournamentInvoker doBuild(final Client client, final GameOnSession gameOnSession) {
        return new V1GameOnPlayerTournamentInvoker(client, gameOnSession);
    }

}
