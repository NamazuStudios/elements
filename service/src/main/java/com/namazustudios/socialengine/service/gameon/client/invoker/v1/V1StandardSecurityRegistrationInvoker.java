package com.namazustudios.socialengine.service.gameon.client.invoker.v1;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.GameOnRegistration;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnRegistrationInvoker;
import com.namazustudios.socialengine.service.gameon.client.model.RegisterPlayerResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.namazustudios.socialengine.rt.ResponseCode.OK;
import static com.namazustudios.socialengine.service.gameon.client.Constants.*;
import static javax.ws.rs.client.Entity.entity;

/**
 * Implements Version 1 standard security registration.
 */
public class V1StandardSecurityRegistrationInvoker implements GameOnRegistrationInvoker {

    private static final String PLAYERS_PATH = "players";

    private static final String REGISTER_PATH = "register";

    private final Client client;

    private final GameOnRegistration gameOnRegistration;

    private final GameOnApplicationConfiguration gameOnApplicationConfiguration;

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

        final Response response = client
              .target(BASE_API)
              .path(VERSION_V1).path(PLAYERS_PATH).path(REGISTER_PATH)
              .request()
              .header(X_API_KEY, gameOnApplicationConfiguration.getPublicApiKey())
              .post(entity(null, MediaType.APPLICATION_JSON_TYPE));

        if (OK.getCode() == response.getStatus()){
            throw new InternalException("Failed to make API call with Amazon GameOn:  " + response.getStatus());
        }

        final RegisterPlayerResponse registerPlayerResponse = response.readEntity(RegisterPlayerResponse.class);
        gameOnRegistration.setPlayerToken(registerPlayerResponse.getPlayerToken());
        gameOnRegistration.setExternalPlayerId(registerPlayerResponse.getExternalPlayerId());

        return gameOnRegistration;

    }

}
