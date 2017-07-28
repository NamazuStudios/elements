package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.NoSuitableMatchException;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchTimeDelta;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;

import java.util.List;

/**
 * Created by patricktwohig on 7/27/17.
 */
public interface Matchmaker {

    /**
     * Returns the {@link MatchingAlgorithm} implemented by this matchmaker.
     *
     * @return the {@link MatchingAlgorithm}
     */
    MatchingAlgorithm getAlgorithm();

    /**
     * Attempts to find an opponent for the supplied {@link Match} instance.  This will
     * query the database for suitable matches.  This will return a {@link SuccessfulMatchTuple} combining
     * the match of the player and the opponent's match.
     *
     * @param match the {@link Match}
     * @return a {@link SuccessfulMatchTuple} representing a successful match
     *
     * @throws NoSuitableMatchException if there is no suitable match found
     *
     */
    SuccessfulMatchTuple attemptToFindOpponent(final Match match) throws NoSuitableMatchException;

    /**
     * Combines a {@link Match} for both a player and an opponent.  This is used to supply
     * information on a succssful match between players only.
     */
    interface SuccessfulMatchTuple {

        /**
         * The opponent {@link Match}.
         *
         * @return the player match
         */
        Match getPlayerMatch();

        /**
         * The opponent {@link Match}.
         *
         * @return
         */
        Match getOpponentMatch();

        /**
         * A list of {@link MatchTimeDelta<MatchTimeDelta>} instances which were generated
         * as the result of the requested operation.  This may include deltas for both
         * players involved in the match.
         *
         * @return a {@Link List<MatchTimeDelta>}
         */
        List<MatchTimeDelta> getMatchDeltas();

    }

}
