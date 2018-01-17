package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.annotation.Expose;
import com.namazustudios.socialengine.dao.Matchmaker.SuccessfulMatchTuple;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.TimeDelta;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchTimeDelta;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import com.namazustudios.socialengine.model.profile.Profile;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by patricktwohig on 7/20/17.
 */
@Expose(luaModuleName = "namazu.socialengine.dao.match")
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

    /**
     * Gets all {@link MatchTimeDelta} instances after the provided timestamp.  This will filter
     * {@link Match} instances for the supplied player ID.
     *
     * @param playerId as specified by the value of {@link Profile#getId()} of {@link Match#getPlayer()}
     * @param timeStamp the earliest timestamp to fetch
     * @return a {@link List<MatchTimeDelta>} instance which represents all {@link MatchTimeDelta}s which satisfy the criteria.
     */
    List<MatchTimeDelta> getDeltasForPlayerAfter(String playerId, long timeStamp);

    /**
     * Gets all {@link MatchTimeDelta} instances after the provided timestamp.  This will filter
     * {@link Match} instances for the supplied player ID and specific match
     *
     * @param playerId as specified by the value of {@link Profile#getId()} of {@link Match#getPlayer()}
     * @param timeStamp the earliest timestamp to fetch
     * @param matchId the Match ID as determined by {@link Match#getId()}
     * @return a {@link List<MatchTimeDelta>} instance which represents all {@link MatchTimeDelta}s which satisfy the criteria.
     */
    List<MatchTimeDelta> getDeltasForPlayerAfter(String playerId, long timeStamp, String matchId);

    /**
     * Returns a {@link Matchmaker} instance for the supplied {@link MatchingAlgorithm}.
     *
     * @param matchingAlgorithm the requested {@link MatchingAlgorithm}
     * @return a {@link Matchmaker}
     *
     * @throws {@link NotImplementedException} if the supplied algorithm is not supported.
     */
    Matchmaker getMatchmaker(final MatchingAlgorithm matchingAlgorithm);

    /**
     * Finalizes the matching process by flagging the {@link Match} instances for deletion in the supplied
     * {@link SuccessfulMatchTuple} and invoking the finalizer {@link Supplier<String>}.  The supplied
     * {@link Supplier<String>} returns a system-wide unique ID used to process to identify the game
     * that was created as the result of the {@link Match}.  The return value of this method will be assigned to the
     * match using {@link Match#setGameId(String)}.
     *
     * Note that this method guarantees that the supplied {@link Supplier<String>} finalizer will only
     * ever be called once per successful matching tuple as multiple players may attempt to finalize the pairing at
     * the same time.  The return value indicates the affected {@link Match} instances, or returns an emnpty stream
     * if no {@link Match} instances were affected by the finalization.
     *
     * Because both players may not have read the {@link Match}, the involved {@link Match} instances will be marked
     * for timeout and deletion at a later time.
     *
     * @param successfulMatchTuple the resulting {@link SuccessfulMatchTuple}
     * @param finalizer the {@linK Supplier<String>} used to finalize the match and provide the resulting game id
     * @return a {@link Stream<TimeDeltaTuple>} of updates
     */
    Stream<TimeDeltaTuple> finalize(SuccessfulMatchTuple successfulMatchTuple, Supplier<String> finalizer);

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
