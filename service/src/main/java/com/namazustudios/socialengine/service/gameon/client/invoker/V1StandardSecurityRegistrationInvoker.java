package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.GameOnRegistration;
import com.namazustudios.socialengine.service.gameon.client.model.RegisterPlayerResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.service.gameon.client.Constants.BASE_API;
import static com.namazustudios.socialengine.service.gameon.client.Constants.VERSION_V1;
import static com.namazustudios.socialengine.service.gameon.client.Constants.X_API_KEY;
import static javax.ws.rs.client.Entity.entity;

/**
 * Implements Version 1 standard security registration.
 */
public class V1StandardSecurityRegistrationInvoker implements GameOnRegistrationInvoker {

    private Client client;

    private GameOnRegistration gameOnRegistration;

    private GameOnApplicationConfiguration gameOnApplicationConfiguration;

    public V1StandardSecurityRegistrationInvoker(
            final Client client,
            final GameOnRegistration gameOnRegistration,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        this.client = client;
        this.gameOnRegistration = gameOnRegistration;
        this.gameOnApplicationConfiguration = gameOnApplicationConfiguration;
    }

    @Override
    public GameOnRegistration invoke() {

        final RegisterPlayerResponse registerPlayerResponse = client
              .target(BASE_API)
              .path(VERSION_V1)
              .request()
              .header(X_API_KEY, gameOnApplicationConfiguration.getPublicApiKey())
              .post(entity(new Object(), MediaType.APPLICATION_JSON_TYPE))
              .readEntity(RegisterPlayerResponse.class);

        gameOnRegistration.setPlayerToken(registerPlayerResponse.getPlayerToken());
        gameOnRegistration.setExternalPlayerId(registerPlayerResponse.getExternalPlayerId());

        return gameOnRegistration;

    }

}
