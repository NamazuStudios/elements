package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.*;

import java.util.List;

public interface GameOnMatchInvoker {

    List<GameOnMatchDetail> getDetail(String tournamentId);

    List<GameOnMatchSummary> getSummaries(TournamentFilter filterBy, String playerAttributes);

    interface Builder extends PlayerRequestBuilder<GameOnMatchInvoker> {}

}
