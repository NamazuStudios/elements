package com.namazustudios.socialengine.service.gameon.client.invoker.v1;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.gameon.GameOnTournamentNotFoundException;
import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnPlayerTournamentInvoker;
import com.namazustudios.socialengine.service.gameon.client.model.TournamentListResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.service.gameon.client.Constants.*;
import static com.namazustudios.socialengine.service.gameon.client.Constants.SESSION_ID;
import static com.namazustudios.socialengine.service.gameon.client.Constants.X_API_KEY;
import static java.util.Collections.emptyList;
import static javax.ws.rs.core.Response.Status.*;

public class V1GameOnPlayerTournamentInvoker implements GameOnPlayerTournamentInvoker {

    public static final String PLAYER_TOURNAMENTS_PATH = "player-tournaments";

    public static final String PERIOD = "period";

    public static final String FILTER_BY = "filterBy";

    public static final String PLAYER_ATTRIBUTES = "playerAttributes";

    private final Client client;

    private final GameOnSession gameOnSession;

    public V1GameOnPlayerTournamentInvoker(final Client client, final GameOnSession gameOnSession) {
        this.client = client;
        this.gameOnSession = gameOnSession;
    }

    @Override
    public GameOnTournamentDetail getDetail(String playerAttributes, final String tournamentId) {

        WebTarget target = client
                .target(BASE_API)
                .path(VERSION_V1).path(PLAYER_TOURNAMENTS_PATH).path(tournamentId);

        if (playerAttributes != null)   target = target.queryParam(PLAYER_ATTRIBUTES, playerAttributes);

        final Response response = target
                .request()
                .header(SESSION_ID, gameOnSession.getSessionId())
                .header(X_API_KEY, gameOnSession.getSessionApiKey())
                .get();

        return get(response, GameOnTournamentDetail.class, () -> null);

    }

    @Override
    public List<GameOnTournamentSummary> getSummaries(
            final TournamentFilter filterBy,
            final TournamentPeriod period,
            final String playerAttributes) {

        WebTarget target = client
            .target(BASE_API)
            .path(VERSION_V1).path(PLAYER_TOURNAMENTS_PATH);

        if (period != null)             target = target.queryParam(PERIOD, period);
        if (filterBy != null)           target = target.queryParam(FILTER_BY, filterBy);
        if (playerAttributes != null)   target = target.queryParam(PLAYER_ATTRIBUTES, playerAttributes);

        final Response response = target
            .request()
            .header(SESSION_ID, gameOnSession.getSessionId())
            .header(X_API_KEY, gameOnSession.getSessionApiKey())
            .get();

        return get(response, TournamentListResponse.class, () -> {
            final TournamentListResponse empty = new TournamentListResponse();
            empty.setTournaments(emptyList());
            return empty;
        }).getTournaments();

    }

    private <ResponseEntityT> ResponseEntityT get(final Response response,
                                                  final Class<ResponseEntityT> responseEntityTClass,
                                                  final Supplier<ResponseEntityT> emptyResponseSupplier) {
        if (OK.getStatusCode() == response.getStatus()) {
            return response.readEntity(responseEntityTClass);
        }  else if (NO_CONTENT.getStatusCode() == response.getStatus()) {
            return emptyResponseSupplier.get();
        } else if (NOT_FOUND.getStatusCode() == response.getStatus()) {
            throw new GameOnTournamentNotFoundException("Player Tournament not found.");
        } else if (FORBIDDEN.getStatusCode() == response.getStatus()) {
            throw new ForbiddenException("Player forbidden by GameOn");
        } else {
            throw new InternalException("Unknown exception interacting with GameOn.");
        }
    }

}
