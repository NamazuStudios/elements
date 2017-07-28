package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.exception.NoSuitableMatchException;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;

/**
 * Created by patricktwohig on 7/27/17.
 */
public class MongoFIFOMatchmaker implements Matchmaker {

    @Override
    public MatchingAlgorithm getAlgorithm() {
        return MatchingAlgorithm.FIFO;
    }

    @Override
    public SuccessfulMatchTuple attemptToFindOpponent(Match match) throws NoSuitableMatchException {
        throw new NotImplementedException();
    }

}
