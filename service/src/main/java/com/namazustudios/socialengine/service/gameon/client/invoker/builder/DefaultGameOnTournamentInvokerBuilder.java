package com.namazustudios.socialengine.service.gameon.client.invoker.builder;

import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnTournamentInvoker;
import com.namazustudios.socialengine.service.gameon.client.invoker.v1.V1GameOnTournamentInvoker;

import javax.ws.rs.client.Client;

public class DefaultGameOnTournamentInvokerBuilder extends AbstractPlayerRequestBuilder<GameOnTournamentInvoker>
                                                   implements GameOnTournamentInvoker.Builder {

    @Override
    protected GameOnTournamentInvoker doBuild(final Client client, final GameOnSession gameOnSession) {
        return new V1GameOnTournamentInvoker(client, gameOnSession);
    }

}
