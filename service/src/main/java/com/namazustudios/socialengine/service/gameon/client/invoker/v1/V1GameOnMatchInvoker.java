package com.namazustudios.socialengine.service.gameon.client.invoker.v1;

import com.namazustudios.socialengine.exception.ConflictException;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.gameon.GameOnMatchNotFoundException;
import com.namazustudios.socialengine.exception.gameon.GameOnTournamentNotFoundException;
import com.namazustudios.socialengine.model.gameon.game.GameOnEnterMatchResponse;
import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.service.gameon.client.exception.PlayerSessionExpiredException;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnMatchInvoker;
import com.namazustudios.socialengine.service.gameon.client.model.EnterMatchRequest;
import com.namazustudios.socialengine.service.gameon.client.model.ErrorResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import java.util.function.Supplier;

import static com.namazustudios.socialengine.GameOnConstants.*;
import static java.util.Collections.emptyList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.CONFLICT;

public class V1GameOnMatchInvoker implements GameOnMatchInvoker {

    public static final String MATCHES_PATH = "matches";

    public static final String ENTER = "enter";

    public static final String PERIOD = "period";

    public static final String FILTER_BY = "filterBy";

    public static final String PLAYER_ATTRIBUTES = "playerAttributes";

    private final Client client;

    private final GameOnSession gameOnSession;

    public V1GameOnMatchInvoker(final Client client, final GameOnSession gameOnSession) {
        this.client = client;
        this.gameOnSession = gameOnSession;
    }

    @Override
    public GameOnMatchDetail getDetail(final String matchId,
                                       final String playerAttributes) {

        WebTarget target = client
            .target(BASE_API)
            .path(VERSION_V1).path(MATCHES_PATH).path(matchId);

        if (playerAttributes != null)   target = target.queryParam(PLAYER_ATTRIBUTES, playerAttributes);

        final Response response = target
            .request()
            .header(SESSION_ID, gameOnSession.getSessionId())
            .header(X_API_KEY, gameOnSession.getSessionApiKey())
            .get();

        return get(response, GameOnMatchDetail.class, () -> null);

    }

    @Override
    public GameOnMatchesAggregate getSummaries(final MatchFilter filterBy,
                                               final MatchType matchType,
                                               final TournamentPeriod period,
                                               final String playerAttributes) {

        WebTarget target = client
            .target(BASE_API)
            .path(VERSION_V1).path(MATCHES_PATH);

        if (period != null)             target = target.queryParam(PERIOD, period);
        if (filterBy != null)           target = target.queryParam(FILTER_BY, filterBy);
        if (playerAttributes != null)   target = target.queryParam(PLAYER_ATTRIBUTES, playerAttributes);

        final Response response = target
            .request()
            .header(SESSION_ID, gameOnSession.getSessionId())
            .header(X_API_KEY, gameOnSession.getSessionApiKey())
            .get();

        return get(response, GameOnMatchesAggregate.class, () -> {
            final GameOnMatchesAggregate empty = new GameOnMatchesAggregate();
            empty.setMatches(emptyList());
            empty.setPlayerMatches(emptyList());
            return empty;
        });

    }

    @Override
    public GameOnEnterMatchResponse postEnterMatch(String matchId, final EnterMatchRequest enterMatchRequest) {

        final Invocation.Builder builder = client
            .target(BASE_API)
            .path(VERSION_V1).path(MATCHES_PATH).path("{matchId}").path(ENTER)
            .resolveTemplate("matchId", matchId)
            .request()
            .header(SESSION_ID, gameOnSession.getSessionId())
            .header(X_API_KEY, gameOnSession.getSessionApiKey());

        final Response response = enterMatchRequest.getPlayerAttributes() == null ?
                builder.post(null) :
                builder.post(entity(enterMatchRequest, APPLICATION_JSON_TYPE));

        if (OK.getStatusCode() == response.getStatus()) {
            return response.readEntity(GameOnEnterMatchResponse.class);
        }

        final ErrorResponse error = response.readEntity(ErrorResponse.class);

        if (BAD_REQUEST.getStatusCode() == response.getStatus()) {
            throw new InvalidParameterException("Supplied invalid parameter: " + error.getMessage());
        }  else if (FORBIDDEN.getStatusCode() == response.getStatus()) {
            throw new ForbiddenException("Player forbidden by GameOn: " + error.getMessage());
        } else if (NOT_FOUND.getStatusCode() == response.getStatus()) {
            throw new GameOnMatchNotFoundException("Match not found: " + error.getMessage());
        } else if (CONFLICT.getStatusCode() == response.getStatus()) {
            throw new ConflictException("Could not enter GameOn tournament: " + error.getMessage());
        } else if (UNAUTHORIZED.getStatusCode() == response.getStatus()) {
            throw new PlayerSessionExpiredException(gameOnSession, error);
        } else {
            throw new InternalException("Unknown exception interacting with GameOn: " + error.getMessage());
        }

    }

    private <ResponseEntityT> ResponseEntityT get(final Response response,
                                                  final Class<ResponseEntityT> responseEntityTClass,
                                                  final Supplier<ResponseEntityT> emptyResponseSupplier) {
        if (OK.getStatusCode() == response.getStatus()) {
            return response.readEntity(responseEntityTClass);
        } else if (NO_CONTENT.getStatusCode() == response.getStatus()) {
            return emptyResponseSupplier.get();
        }

        final ErrorResponse error = response.readEntity(ErrorResponse.class);

        if (NOT_FOUND.getStatusCode() == response.getStatus()) {
            throw new GameOnTournamentNotFoundException("GameOn Match not found: " + error.getMessage());
        } else if (FORBIDDEN.getStatusCode() == response.getStatus()) {
            throw new ForbiddenException("Player forbidden by GameOn: " + error.getMessage());
        } else if (UNAUTHORIZED.getStatusCode() == response.getStatus()) {
            throw new PlayerSessionExpiredException(gameOnSession, error);
        } else {
            throw new InternalException("Unknown exception interacting with GameOn: " + error.getMessage());
        }

    }

}
