package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.GameOnTournamentDetail;
import com.namazustudios.socialengine.model.gameon.GameOnTournamentSummary;
import com.namazustudios.socialengine.model.gameon.TournamentFilter;

import java.util.List;

public interface GameOnTournamentInvoker {

    List<GameOnTournamentSummary> getSummaries(TournamentFilter filterBy, String playerAttributes);

    List<GameOnTournamentDetail> getDetail(String tournamentId);

    interface Builder extends PlayerRequestBuilder<GameOnTournamentInvoker> {}

}
