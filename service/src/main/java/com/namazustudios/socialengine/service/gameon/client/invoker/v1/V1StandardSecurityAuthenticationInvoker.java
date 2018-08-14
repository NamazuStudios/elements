package com.namazustudios.socialengine.service.gameon.client.invoker.v1;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.game.GameOnRegistration;
import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnAuthenticationInvoker;
import com.namazustudios.socialengine.service.gameon.client.model.AuthPlayerRequest;
import com.namazustudios.socialengine.service.gameon.client.model.AuthPlayerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static com.namazustudios.socialengine.service.gameon.client.Constants.BASE_API;
import static com.namazustudios.socialengine.service.gameon.client.Constants.VERSION_V1;
import static com.namazustudios.socialengine.service.gameon.client.Constants.X_API_KEY;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;

public class V1StandardSecurityAuthenticationInvoker implements GameOnAuthenticationInvoker {

    private static final Logger logger = LoggerFactory.getLogger(V1StandardSecurityRegistrationInvoker.class);

    private static final String PLAYERS_PATH = "players";

    private static final String AUTH_PATH = "auth";

    private final Client client;

    private final GameOnSession gameOnSession;

    private final GameOnRegistration gameOnRegistration;

    private final GameOnApplicationConfiguration gameOnApplicationConfiguration;

    public V1StandardSecurityAuthenticationInvoker(
            final Client client,
            final GameOnSession gameOnSession,
            final GameOnRegistration gameOnRegistration,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        this.client = client;
        this.gameOnSession = gameOnSession;
        this.gameOnRegistration = gameOnRegistration;
        this.gameOnApplicationConfiguration = gameOnApplicationConfiguration;
    }

    @Override
    public GameOnSession invoke() {

        final AuthPlayerRequest authPlayerRequest = new AuthPlayerRequest();
        authPlayerRequest.setAppBuildType(gameOnSession.getAppBuildType());
        authPlayerRequest.setDeviceOSType(gameOnSession.getDeviceOSType());
        authPlayerRequest.setPlayerName(gameOnSession.getProfile().getDisplayName());
        authPlayerRequest.setPlayerToken(gameOnRegistration.getPlayerToken());

        final Response response = client
            .target(BASE_API)
            .path(VERSION_V1).path(PLAYERS_PATH).path(AUTH_PATH)
            .request()
            .header(X_API_KEY, gameOnApplicationConfiguration.getPublicApiKey())
            .post(entity(authPlayerRequest, APPLICATION_JSON_TYPE));

        if (OK.getStatusCode() != response.getStatus()) {
            // Anything but okay is either a misconfiguration, error on Amazon's end, Network failure etc.
            logger.error("GameOn returned error status {}", response.getStatus());
            throw new InternalException("Failed to make API call with Amazon GameOn:  " + response.getStatus());
        }

        final AuthPlayerResponse authPlayerResponse = response.readEntity(AuthPlayerResponse.class);
        gameOnSession.setSessionId(authPlayerResponse.getSessionId());
        gameOnSession.setSessionApiKey(authPlayerResponse.getSessionApiKey());
        gameOnSession.setSessionExpirationDate(authPlayerResponse.getSessionExpirationDate());

        return gameOnSession;

    }

}
