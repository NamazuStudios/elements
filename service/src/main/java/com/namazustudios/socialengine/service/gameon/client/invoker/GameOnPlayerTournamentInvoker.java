package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.*;
import com.namazustudios.socialengine.service.gameon.client.model.EnterPlayerTournamentRequest;

import java.util.List;

public interface GameOnPlayerTournamentInvoker {

    GameOnTournamentDetail getDetail(String playerAttributes, String tournamentId);

    List<GameOnTournamentSummary> getSummaries(
            TournamentFilter filterBy,
            TournamentPeriod period,
            String playerAttributes);

    GameOnPlayerTournamentEnterResponse postEnterRequest(
            String tournamentId,
            EnterPlayerTournamentRequest enterPlayerTournamentRequest);

    interface Builder extends PlayerRequestBuilder<GameOnPlayerTournamentInvoker> {}

}
