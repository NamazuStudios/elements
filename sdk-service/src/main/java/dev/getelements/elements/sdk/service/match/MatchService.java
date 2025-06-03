package dev.getelements.elements.sdk.service.match;

import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.match.Match;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.service.topic.Topic;

import java.util.function.Consumer;

/**
 * Manages instances of {@link Match}.  This is responsible for retrieving data from the database
 * as well as firing off notifications when {@link Match} instances change, or need to be updated.
 *
 * Created by patricktwohig on 7/19/17.
 */
@ElementPublic
@ElementServiceExport
@ElementEventProducer(
        value = MatchService.EVENT_1V1_MADE,
        parameters = {Profile.class, Profile.class},
        description = "Event indicating that a simple 1v1 match has been made between two profiles."
)
public interface MatchService {

    /**
     * A match-made event. Indicates that a 1v1 quick match was made.
     */
    String EVENT_1V1_MADE = "dev.getelements.elements.service.match.1v1.made";

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
     * Waits for a {@link Match} to be updated by another request.  Upon update, this will pass the update into the
     * supplied supplied {@link Consumer}.  In the event the {@link Match} has been deleted, the {@link Consumer} will
     * receive a null value indicating so.
     *
     * The returned {@link Subscription} need not be closed, unless explicitly requesting un-subscription.
     * {@see {@link Subscription#subscribeNext(Consumer, Consumer)}}.
     *
     * @param matchId the match ID {@link Match#getId()}.
     * @param matchConsumer a {@link Consumer<Match>} used to receive the {@link Match} instance
     * @param exceptionConsumer a {@link Consumer<Exception>} used to receive any error encountered in the process
     *
     * @return a {@link Runnable} which may be used to cancel the pending request
     */
    Topic.Subscription attemptRematchAndPoll(String matchId, Consumer<Match> matchConsumer, Consumer<Exception> exceptionConsumer);

}
