package com.namazustudios.socialengine.service.gameon.client.invoker.v1;

import com.namazustudios.socialengine.model.gameon.GameOnMatchDetail;
import com.namazustudios.socialengine.model.gameon.GameOnMatchSummary;
import com.namazustudios.socialengine.model.gameon.GameOnSession;
import com.namazustudios.socialengine.model.gameon.TournamentFilter;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnMatchInvoker;

import javax.ws.rs.client.Client;
import java.util.List;

public class V1GameOnMatchInvoker implements GameOnMatchInvoker {

    private static final String MATCH_PATH = "matches";

    private final Client client;

    private final GameOnSession gameOnSession;

    public V1GameOnMatchInvoker(final Client client, final GameOnSession gameOnSession) {
        this.client = client;
        this.gameOnSession = gameOnSession;
    }

    @Override
    public List<GameOnMatchDetail> getDetail(final String tournamentId) {
        // TODO
        return null;
    }

    @Override
    public List<GameOnMatchSummary> getSummaries(final TournamentFilter filterBy, final String playerAttributes) {
        // TODO
        return null;
    }

}

