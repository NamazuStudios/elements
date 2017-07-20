package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Match;
import com.namazustudios.socialengine.model.Pagination;

import java.util.Date;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Manages instances of {@link Match}.  This is responsible for retrieving data from the database
 * as well as firing off notifications when {@link Match} instances change, or need to be updated.
 *
 * Created by patricktwohig on 7/19/17.
 */
public interface MatchService {

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
     *
     *
     * @param offset
     * @param count
     * @param lastUpdatedSince
     *
     * @return
     */
    Pagination<Match> getMatches(int offset, int count, Date lastUpdatedSince);

    /**
     *
     * @param lastUpdatedSince
     * @param matchStreamConsumer
     * @param exceptionConsumer
     */
    void waitForUpdates(Date lastUpdatedSince,
                        Consumer<Stream<Match>> matchStreamConsumer,
                        Consumer<Exception> exceptionConsumer);

}
