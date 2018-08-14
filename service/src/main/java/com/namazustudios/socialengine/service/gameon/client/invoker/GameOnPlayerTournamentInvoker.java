package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.game.GameOnTournamentDetail;
import com.namazustudios.socialengine.model.gameon.game.GameOnTournamentSummary;
import com.namazustudios.socialengine.model.gameon.game.TournamentFilter;
import com.namazustudios.socialengine.model.gameon.game.TournamentPeriod;

import java.util.List;

public interface GameOnPlayerTournamentInvoker {

    GameOnTournamentDetail getDetail(String playerAttributes, String tournamentId);

    List<GameOnTournamentSummary> getSummaries(
            TournamentFilter filterBy,
            TournamentPeriod period,
            String playerAttributes);

    interface Builder extends PlayerRequestBuilder<GameOnPlayerTournamentInvoker> {}

}
