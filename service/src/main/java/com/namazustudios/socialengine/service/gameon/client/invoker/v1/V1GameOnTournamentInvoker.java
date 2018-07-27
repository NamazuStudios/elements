package com.namazustudios.socialengine.service.gameon.client.invoker.v1;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.gameon.GameOnTournamentNotFoundException;
import com.namazustudios.socialengine.model.gameon.*;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnTournamentInvoker;
import com.namazustudios.socialengine.service.gameon.client.model.GameOnTournamentListResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.namazustudios.socialengine.rt.ResponseCode.OK;
import static com.namazustudios.socialengine.service.gameon.client.Constants.BASE_API;
import static com.namazustudios.socialengine.service.gameon.client.Constants.SESSION_ID;
import static com.namazustudios.socialengine.service.gameon.client.Constants.X_API_KEY;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

public class V1GameOnTournamentInvoker implements GameOnTournamentInvoker {

    public static final String TOURNAMENTS_PATH = "tournaments";

    public static final String PERIOD = "period";

    public static final String FILTER_BY = "filterBy";

    public static final String PLAYER_ATTRIBUTES = "playerAttributes";

    private final Client client;

    private final GameOnSession gameOnSession;

    public V1GameOnTournamentInvoker(final Client client, final GameOnSession gameOnSession) {
        this.client = client;
        this.gameOnSession = gameOnSession;
    }

    @Override
    public GameOnTournamentDetail getDetail(String playerAttributes, final String tournamentId) {

        WebTarget target = client
            .target(BASE_API)
            .path(TOURNAMENTS_PATH).path(tournamentId);

        if (playerAttributes != null)   target = target.queryParam(PLAYER_ATTRIBUTES, playerAttributes);

        final Response response = target
            .request()
            .header(SESSION_ID, gameOnSession.getSessionId())
            .header(X_API_KEY, gameOnSession.getSessionApiKey())
            .get();

        return get(response, GameOnTournamentDetail.class);

    }

    @Override
    public List<GameOnTournamentSummary> getSummaries(
            final TournamentFilter filterBy,
            final TournamentPeriod period,
            final String playerAttributes) {

        WebTarget target = client
            .target(BASE_API)
            .path(TOURNAMENTS_PATH);

        if (period != null)             target = target.queryParam(PERIOD, period);
        if (filterBy != null)           target = target.queryParam(FILTER_BY, filterBy);
        if (playerAttributes != null)   target = target.queryParam(PLAYER_ATTRIBUTES, playerAttributes);

        final Response response = target
            .request()
            .header(SESSION_ID, gameOnSession.getSessionId())
            .header(X_API_KEY, gameOnSession.getSessionApiKey())
            .get();

        return get(response, GameOnTournamentListResponse.class).getTournaments();

    }

    private <ResponseEntityT> ResponseEntityT get(final Response response,
                                                  final Class<ResponseEntityT> responseEntityTClass) {
        if (OK.getCode() == response.getStatus()) {
            return response.readEntity(responseEntityTClass);
        } else if (NOT_FOUND.getStatusCode() == response.getStatus()) {
            throw new GameOnTournamentNotFoundException("Tournament not found.");
        } else if (FORBIDDEN.getStatusCode() == response.getStatus()) {
            throw new ForbiddenException("Player forbidden by GameOn");
        } else {
            throw new InternalException("Unknown exception interacting with GameOn.");
        }
    }

}
