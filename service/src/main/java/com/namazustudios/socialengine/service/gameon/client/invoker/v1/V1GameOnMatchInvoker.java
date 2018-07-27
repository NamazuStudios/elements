package com.namazustudios.socialengine.service.gameon.client.invoker.v1;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.gameon.GameOnTournamentNotFoundException;
import com.namazustudios.socialengine.model.gameon.*;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnMatchInvoker;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static com.namazustudios.socialengine.rt.ResponseCode.OK;
import static com.namazustudios.socialengine.service.gameon.client.Constants.BASE_API;
import static com.namazustudios.socialengine.service.gameon.client.Constants.SESSION_ID;
import static com.namazustudios.socialengine.service.gameon.client.Constants.X_API_KEY;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

public class V1GameOnMatchInvoker implements GameOnMatchInvoker {

    public static final String MATCHES_PATH = "matches";

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
            .path(MATCHES_PATH).path(matchId);

        if (playerAttributes != null)   target = target.queryParam(PLAYER_ATTRIBUTES, playerAttributes);

        final Response response = target
            .request()
            .header(SESSION_ID, gameOnSession.getSessionId())
            .header(X_API_KEY, gameOnSession.getSessionApiKey())
            .get();

        return get(response, GameOnMatchDetail.class);

    }

    @Override
    public GameOnMatchesAggregate getSummaries(final MatchFilter filterBy,
                                               final MatchType matchType,
                                               final TournamentPeriod period,
                                               final String playerAttributes) {

        WebTarget target = client
            .target(BASE_API)
            .path(MATCHES_PATH);

        if (period != null)             target = target.queryParam(PERIOD, period);
        if (filterBy != null)           target = target.queryParam(FILTER_BY, filterBy);
        if (playerAttributes != null)   target = target.queryParam(PLAYER_ATTRIBUTES, playerAttributes);

        final Response response = target
            .request()
            .header(SESSION_ID, gameOnSession.getSessionId())
            .header(X_API_KEY, gameOnSession.getSessionApiKey())
            .get();

        return get(response, GameOnMatchesAggregate.class);

    }

    private <ResponseEntityT> ResponseEntityT get(final Response response,
                                                  final Class<ResponseEntityT> responseEntityTClass) {
        if (OK.getCode() == response.getStatus()) {
            return response.readEntity(responseEntityTClass);
        } else if (NOT_FOUND.getStatusCode() == response.getStatus()) {
            throw new GameOnTournamentNotFoundException("Match not found.");
        } else if (FORBIDDEN.getStatusCode() == response.getStatus()) {
            throw new ForbiddenException("Player forbidden by GameOn");
        } else {
            throw new InternalException("Unknown exception interacting with GameOn.");
        }
    }

}
