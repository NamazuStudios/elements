package com.namazustudios.socialengine.service.gameon.client.invoker.v1;

import com.namazustudios.socialengine.model.gameon.GameOnSession;
import com.namazustudios.socialengine.model.gameon.GameOnTournamentDetail;
import com.namazustudios.socialengine.model.gameon.GameOnTournamentSummary;
import com.namazustudios.socialengine.model.gameon.TournamentFilter;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnTournamentInvoker;

import javax.ws.rs.client.Client;
import java.util.List;

public class V1GameOnTournamentInvoker implements GameOnTournamentInvoker {

    private final Client client;

    private final GameOnSession gameOnSession;

    public V1GameOnTournamentInvoker(final Client client, final GameOnSession gameOnSession) {
        this.client = client;
        this.gameOnSession = gameOnSession;
    }

    @Override
    public List<GameOnTournamentSummary> getSummaries(TournamentFilter filterBy, String playerAttributes) {
        // TODO Implement this.
        return null;
    }

    @Override
    public List<GameOnTournamentDetail> getDetail(String tournamentId) {
        return null;
    }

}
