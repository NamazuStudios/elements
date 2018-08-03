package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.*;
import com.namazustudios.socialengine.service.gameon.client.model.EnterMatchRequest;

import java.util.List;

public interface GameOnMatchInvoker {

    GameOnMatchDetail getDetail(String matchId, String playerAttributes);

    GameOnMatchesAggregate getSummaries(MatchFilter filterBy,
                                        MatchType matchType,
                                        TournamentPeriod period,
                                        String playerAttributes);

    GameOnEnterMatchResponse enterMatch(EnterMatchRequest enterMatchRequest);

    interface Builder extends PlayerRequestBuilder<GameOnMatchInvoker> {}

}
