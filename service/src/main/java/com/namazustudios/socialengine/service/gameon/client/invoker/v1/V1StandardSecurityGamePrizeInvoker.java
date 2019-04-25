package com.namazustudios.socialengine.service.gameon.client.invoker.v1;

import com.namazustudios.socialengine.GameOnConstants;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.gameon.GameOnPlayerTournamentNotFoundException;
import com.namazustudios.socialengine.model.gameon.game.GameOnClaimPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.game.GameOnFulfillPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.game.GameOnGetPrizeDetailsResponse;
import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.service.gameon.client.exception.PlayerSessionExpiredException;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnGamePrizeInvoker;
import com.namazustudios.socialengine.service.gameon.client.model.ClaimPrizeListRequest;
import com.namazustudios.socialengine.service.gameon.client.model.ErrorResponse;
import com.namazustudios.socialengine.service.gameon.client.model.FulfillPrizeListRequest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Supplier;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

public class V1StandardSecurityGamePrizeInvoker implements GameOnGamePrizeInvoker {

    private static final String PRIZES_PATH = "prizes";

    private static final String CLAIM_PATH = "claim";

    private static final String FULFILL_PATH = "fulfill";

    private final Client client;

    private final GameOnSession gameOnSession;

    public V1StandardSecurityGamePrizeInvoker(final Client client, final GameOnSession gameOnSession) {
        this.client = client;
        this.gameOnSession = gameOnSession;
    }

    @Override
    public GameOnClaimPrizeListResponse claim(final ClaimPrizeListRequest claimPrizeListRequest) {

        final Invocation.Builder builder = client
            .target(GameOnConstants.BASE_API)
            .path(GameOnConstants.VERSION_V1).path(PRIZES_PATH).path(CLAIM_PATH)
            .request()
            .header(GameOnConstants.SESSION_ID, gameOnSession.getSessionId())
            .header(GameOnConstants.X_API_KEY, gameOnSession.getSessionApiKey());

        final Response response =
            claimPrizeListRequest.getAwardedPrizeIds() == null ||
            claimPrizeListRequest.getAwardedPrizeIds().isEmpty() ?
                builder.post(null) :
                builder.post(entity(claimPrizeListRequest, APPLICATION_JSON_TYPE));

        return getResponse(response, GameOnClaimPrizeListResponse.class, () -> {
            final GameOnClaimPrizeListResponse claimPrizeListResponse = new GameOnClaimPrizeListResponse();
            return claimPrizeListResponse;
        });

    }

    @Override
    public GameOnFulfillPrizeListResponse fulfill(final FulfillPrizeListRequest fulfillPrizeListRequest) {

        final Invocation.Builder builder = client
            .target(GameOnConstants.BASE_API)
            .path(GameOnConstants.VERSION_V1).path(PRIZES_PATH).path(FULFILL_PATH)
            .request()
            .header(GameOnConstants.SESSION_ID, gameOnSession.getSessionId())
            .header(GameOnConstants.X_API_KEY, gameOnSession.getSessionApiKey());


        final Response response =
            fulfillPrizeListRequest.getAwardedPrizeIds() == null ||
            fulfillPrizeListRequest.getAwardedPrizeIds().isEmpty() ?
                builder.post(null) :
                builder.post(entity(fulfillPrizeListRequest, APPLICATION_JSON_TYPE));

        return getResponse(response, GameOnFulfillPrizeListResponse.class, () -> {
            final GameOnFulfillPrizeListResponse fulfillPrizeListResponse = new GameOnFulfillPrizeListResponse();
            return fulfillPrizeListResponse;
        });

    }

    @Override
    public GameOnGetPrizeDetailsResponse getDetails(final String prizeId) {
        final Invocation.Builder builder = client
                .target(GameOnConstants.BASE_API)
                .path(GameOnConstants.VERSION_V1).path(PRIZES_PATH).path(prizeId)
                .request()
                .header(GameOnConstants.SESSION_ID, gameOnSession.getSessionId())
                .header(GameOnConstants.X_API_KEY, gameOnSession.getSessionApiKey());

        final Response response = builder.get();

        return getResponse(response, GameOnGetPrizeDetailsResponse.class, () -> {
           final GameOnGetPrizeDetailsResponse prizeDetailsResponse = new GameOnGetPrizeDetailsResponse();
           return prizeDetailsResponse;
        });
    }

    private <T> T getResponse(final Response response,
                              final Class<T> responseEntityTClass,
                              final Supplier<T> emptyResponseSupplier) {

        if (OK.getStatusCode() == response.getStatus()) {
            return response.readEntity(responseEntityTClass);
        } else if (207 == response.getStatus() || NO_CONTENT.getStatusCode() == response.getStatus()) {
            return emptyResponseSupplier.get();
        }

        final ErrorResponse error = response.readEntity(ErrorResponse.class);

        final String errorMessage = error.getMessage() != null ? error.getMessage() : error.getMessage();

        if (FORBIDDEN.getStatusCode() == response.getStatus()) {
            throw new ForbiddenException("Player forbidden by GameOn: " + errorMessage);
        } else if (UNAUTHORIZED.getStatusCode() == response.getStatus()) {
            throw new PlayerSessionExpiredException(gameOnSession, error);
        } else {
            throw new InternalException("Unknown exception interacting with GameOn: " + errorMessage);
        }

    }

}
