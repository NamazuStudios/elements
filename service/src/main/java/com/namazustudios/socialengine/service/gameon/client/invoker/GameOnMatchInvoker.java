package com.namazustudios.socialengine.service.gameon.client.invoker;

import com.namazustudios.socialengine.model.gameon.GameOnEnterMatchResponse;
import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.service.gameon.client.model.EnterMatchRequest;

public interface GameOnMatchInvoker {

    GameOnMatchDetail getDetail(String matchId, String playerAttributes);

    GameOnMatchesAggregate getSummaries(MatchFilter filterBy,
                                        MatchType matchType,
                                        TournamentPeriod period,
                                        String playerAttributes);

    GameOnEnterMatchResponse postEnterMatch(String matchId,
                                            EnterMatchRequest enterMatchRequest);

    interface Builder extends PlayerRequestBuilder<GameOnMatchInvoker> {}

}
