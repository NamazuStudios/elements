package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.TimeDelta;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchTimeDelta;
import com.namazustudios.socialengine.model.profile.Profile;

import java.util.List;

/**
 * Created by patricktwohig on 7/20/17.
 */
public interface MatchDao {

    /**
     * Fetches a {@link Match} with the given profile ID, and match ID.  If no such
     * match is found, then this throws an instance of {@link NotFoundException}.
     *
     * The profile ID is compared against the ID of {@link Match#getPlayer()}
     *
     * @param playerId as specified by the value of {@link Profile#getId()} of {@link Match#getPlayer()}
     * @param matchId the mach id as determined by {@link Match#getId()}
     *
     * @return the {@link Match}, never null
     * @throws {@link NotFoundException} if the match is not found.
     */
    Match getMatchForPlayer(String playerId, String matchId) throws NotFoundException;

    /**
     * Fetches all {@link Match} instances with the given profile ID, and match ID.  If no such
     * match is found, then this throws an instance of {@link NotFoundException}.
     *
     * The profile ID is compared against the ID of {@link Match#getPlayer()}
     *
     * @param playerId as specified by the value of {@link Profile#getId()} of {@link Match#getPlayer()}
     * @param offset the offset
     * @param count the count
     *
     * @return the {@link Match}, never null
     * @throws {@link NotFoundException} if the match is not found.
     */
    Pagination<Match> getMatchesForPlayer(String playerId, int offset, int count);

    /**
     * Fetches all {@link Match} instances with the given profile ID, and match ID.  If no such
     * match is found, then this throws an instance of {@link NotFoundException}.
     *
     * The profile ID is compared against the ID of {@link Match#getPlayer()}
     *
     * @param playerId as specified by the value of {@link Profile#getId()} of {@link Match#getPlayer()}
     * @param offset the offset
     * @param count the count
     * @param search a search query to filter {@link Match} instances
     *
     * @return the {@link Match}, never null
     * @throws {@link NotFoundException} if the match is not found.
     */
    Pagination<Match> getMatchesForPlayer(String playerId, int offset, int count, String search);

    /**
     * Crates an instance of {@linK Match} in the database.  Additionally, this is responsible for lo
     *
     * @param match the {@link Match} to create
     * @return a {@link TimeDeltaTuple} of the {@link Match} as it was written to the database as well as the associated {@link TimeDelta}
     */
    TimeDeltaTuple createMatchAndLogDelta(Match match);

    /**
     * Deletes the instance of {@link Match} with the supplied ID, provided that is is owned
     * by the {@link Profile} with the supplied ID.
     *
     * @param playerId as specified by the value of {@link Profile#getId()} of {@link Match#getPlayer()}
     * @param matchId the match ID itself to delete
     *
     * @return the {@link TimeDelta<String, Match>} that was written as the result of this operation.
     */
    MatchTimeDelta deleteMatchAndLogDelta(String playerId, String matchId);

    List<TimeDelta<String,Match>> getDeltasForPlayerAfter(String profileId, long timeStamp);

    List<TimeDelta<String, Match>> getDeltasForPlayerAfter(String id, long timeStamp, String matchId);

    /**
     * Used as the return value for the various methods tracking {@link TimeDelta} isntances.
     */
    interface TimeDeltaTuple {

        /**
         * Gets the {@link Match} in the tuple.
         *
         * @return the {@link Match}
         */
        Match getMatch();

        /**
         * Gets the {@link TimeDelta<String , Match>} representing the change.
         *
         * @return the {@link TimeDelta<String , Match>}
         */
        MatchTimeDelta getTimeDelta();

    }

}
