package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.*;
import com.namazustudios.socialengine.service.gameon.client.model.EnterTournamentRequest;

import java.util.List;

public interface GameOnTournamentInvoker {

    GameOnTournamentDetail getDetail(String playerAttributes, String tournamentId);

    List<GameOnTournamentSummary> getSummaries(
            TournamentFilter filterBy,
            TournamentPeriod period,
            String playerAttributes);

    GameOnTournamentEnterResponse postEnterRequest(
            String tournamentId,
            EnterTournamentRequest enterTournamentRequest);

    interface Builder extends PlayerRequestBuilder<GameOnTournamentInvoker> {}

}
