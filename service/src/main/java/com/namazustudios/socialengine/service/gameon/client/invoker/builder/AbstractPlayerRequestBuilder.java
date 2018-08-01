package com.namazustudios.socialengine.service.gameon.client.invoker.builder;

import com.namazustudios.socialengine.model.gameon.GameOnSession;
import com.namazustudios.socialengine.service.gameon.client.invoker.PlayerRequestBuilder;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

public abstract class AbstractPlayerRequestBuilder<BuiltT> implements PlayerRequestBuilder<BuiltT> {

    private Client client;

    private GameOnSession gameOnSession;

    @Override
    public PlayerRequestBuilder<BuiltT> withSession(final GameOnSession gameOnSession) {
        this.gameOnSession = gameOnSession;
        return this;
    }

    @Override
    public BuiltT build() {
        if (gameOnSession == null) throw new IllegalStateException("session not specified.");
        if (gameOnSession.getSessionId() == null) throw new IllegalStateException("session id not specified");
        if (gameOnSession.getSessionApiKey() == null) throw new IllegalStateException("session api key not specified");
        return doBuild(getClient(), gameOnSession);
    }

    /**
     * Performs the actual build operation.
     *
     * @param client
     * @param gameOnSession the {@link GameOnSession}
     * @return the type to build
     */
    protected abstract BuiltT doBuild(Client client, GameOnSession gameOnSession);

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
