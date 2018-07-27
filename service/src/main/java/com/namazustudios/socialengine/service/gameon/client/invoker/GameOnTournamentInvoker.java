package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.GameOnTournamentDetail;
import com.namazustudios.socialengine.model.gameon.GameOnTournamentSummary;
import com.namazustudios.socialengine.model.gameon.TournamentFilter;
import com.namazustudios.socialengine.model.gameon.TournamentPeriod;

import java.util.List;

public interface GameOnTournamentInvoker {

    GameOnTournamentDetail getDetail(String playerAttributes, String tournamentId);

    List<GameOnTournamentSummary> getSummaries(TournamentFilter filterBy, TournamentPeriod period, String playerAttributes);

    interface Builder extends PlayerRequestBuilder<GameOnTournamentInvoker> {}

}
