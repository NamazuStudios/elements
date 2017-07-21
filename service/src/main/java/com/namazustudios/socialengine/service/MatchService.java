package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.TimeDelta;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchTimeDelta;

import java.util.List;
import java.util.function.Consumer;

/**
 * Manages instances of {@link Match}.  This is responsible for retrieving data from the database
 * as well as firing off notifications when {@link Match} instances change, or need to be updated.
 *
 * Created by patricktwohig on 7/19/17.
 */
public interface MatchService {

    /**
     * Gets the {@link Match} with the specified id.
     *
     * @param matchId the Match ID as specified by {@link Match#getId()}
     *
     * @return the {@link Match}
     */
    Match getMatch(String matchId);

    /**
     * Gets all matches.
     *
     * @param offset the offset
     * @param count the count
     *
     * @return a {@link Pagination<Match>} instance containing the requested data
     */
    Pagination<Match> getMatches(int offset, int count);

    /**
     * Gets all matches, specifying search criteria.
     *
     * @param offset the offset
     * @param count the count
     *
     * @return a {@link Pagination<Match>} instance containing the requested data
     */
    Pagination<Match> getMatches(int offset, int count, String search);

    /**
     * Creates a {@link Match}.  The supplied {@link Match} should have no opponent.
     *
     * @param match the {@link Match} object
     * @return the {@link Match}, as it was written to the database
     */
    Match createMatch(Match match);

    /**
     * Deletes a {@link Match} with the supplied ID, as determined by {@link Match#getId()}.
     *
     * @param matchId the match ID
     */
    void deleteMatch(String matchId);

    /**
     * Gets a list of {@link TimeDelta <String, Match>} instances starting at the timestamp.
     *
     * @param timeStamp timeStamp
     *
     * @return all availble {@link TimeDelta} instances
     */
    List<TimeDelta<String, Match>> getDeltas(long timeStamp);

    /**
     * Gets a lists of {@link TimeDelta<String, Match>} instances starting at the timestamp
     * for the {@link Match} with the particular ID.
     *
     * @param timeStamp the timestamp of the earliest delta to find
     * @param matchId the match ID as represented by {@link Match#getId()}
     *
     * @return a {@link List<TimeDelta<String, Match>>} instances.
     */
    List<TimeDelta<String,Match>> getDeltasForMatch(long timeStamp, String matchId);

    /**
     * Waits for a {@link List<TimeDelta<String, Match>>} to become available as changes are
     * made to {@link Match} instances.  This will listen for changes to all {@link Match}
     * instances.
     *
     * @param timeStamp timeStamp
     *
     * @param timeDeltaListConsumer
     * @return a {@link Runnable} which may be used to cancel the pending request
     */
    Runnable waitForDeltas(long timeStamp,
                           final Consumer<List<MatchTimeDelta>> timeDeltaListConsumer,
                           final Consumer<Exception> exceptionConsumer);

    /**
     * Waits for a {@link List<TimeDelta<String, Match>>} to become available as changes are
     * made to {@link Match} instances.  This will listen for changes to a specific {@link Match}
     * instance.
     *
     * @param timeStamp timeStamp
     * @param matchId the Match ID as specified by {@link Match#getId()}
     *
     * @param timeDeltaListConsumer
     * @return a {@link Runnable} which may be used to cancel the pending request
     */
    Runnable waitForDeltas(long timeStamp, String matchId,
                           final Consumer<List<MatchTimeDelta>> timeDeltaListConsumer,
                           final Consumer<Exception> exceptionConsumer);

}
