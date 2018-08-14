package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.game.*;

public interface GameOnMatchInvoker {

    GameOnMatchDetail getDetail(String matchId, String playerAttributes);

    GameOnMatchesAggregate getSummaries(MatchFilter filterBy,
                                        MatchType matchType,
                                        TournamentPeriod period,
                                        String playerAttributes);

    interface Builder extends PlayerRequestBuilder<GameOnMatchInvoker> {}

}
