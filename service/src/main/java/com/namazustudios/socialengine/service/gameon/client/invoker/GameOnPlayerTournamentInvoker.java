package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.GameOnPlayerTournamentEnterResponse;
import com.namazustudios.socialengine.model.gameon.game.GameOnTournamentDetail;
import com.namazustudios.socialengine.model.gameon.game.GameOnTournamentSummary;
import com.namazustudios.socialengine.model.gameon.game.TournamentFilter;
import com.namazustudios.socialengine.model.gameon.game.TournamentPeriod;
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
