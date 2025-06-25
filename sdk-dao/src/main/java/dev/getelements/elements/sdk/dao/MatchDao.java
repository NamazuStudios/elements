package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.NotImplementedException;
import dev.getelements.elements.sdk.model.match.Match;
import dev.getelements.elements.sdk.model.profile.Profile;

/**
 * Created by patricktwohig on 7/20/17.
 */

@ElementServiceExport
public interface MatchDao {

    /**
     * Fetches a {@link Match} with the given profile ID, and match ID.  If no such
     * match is found, then this throws an instance of {@link NotFoundException}.
     * <p>
     * The profile ID is compared against the ID of {@link Match#getPlayer()}
     *
     * @param playerId as specified by the value of {@link Profile#getId()} of {@link Match#getPlayer()}
     * @param matchId  the mach id as determined by {@link Match#getId()}
     * @return the {@link Match}, never null
     * @throws NotFoundException if the match is not found.
     */
    Match getMatchForPlayer(String playerId, String matchId) throws NotFoundException;

    /**
     * Fetches all {@link Match} instances with the given profile ID, and match ID.  If no such
     * match is found, then this throws an instance of {@link NotFoundException}.
     * <p>
     * The profile ID is compared against the ID of {@link Match#getPlayer()}
     *
     * @param playerId as specified by the value of {@link Profile#getId()} of {@link Match#getPlayer()}
     * @param offset   the offset
     * @param count    the count
     * @return the {@link Match}, never null
     * @throws NotFoundException if the match is not found.
     */
    Pagination<Match> getMatchesForPlayer(String playerId, int offset, int count);

    /**
     * Fetches all {@link Match} instances with the given profile ID, and match ID.  If no such
     * match is found, then this throws an instance of {@link NotFoundException}.
     * <p>
     * The profile ID is compared against the ID of {@link Match#getPlayer()}
     *
     * @param playerId as specified by the value of {@link Profile#getId()} of {@link Match#getPlayer()}
     * @param offset   the offset
     * @param count    the count
     * @param search   a search query to filter {@link Match} instances
     * @return the {@link Match}, never null
     * @throws NotFoundException if the match is not found.
     */
    Pagination<Match> getMatchesForPlayer(String playerId, int offset, int count, String search);

    /**
     * Crates an instance of {@link Match} in the database.  Note that this does not
     *
     * @param match the {@link Match} to create
     * @return a {@link Match} as it was written to the database
     */
    Match createMatch(Match match);

    /**
     * Returns the default {@link Matchmaker} instance.
     *
     * @return a {@link Matchmaker}
     * @throws NotImplementedException if the supplied algorithm is not supported.
     */
    Matchmaker getDefaultMatchmaker();

    /**
     * Deletes a {@link Match}, specyifying the id of the {@link Match}.  Note that a {@link Match} may not be deleted
     * if it is already matched to an opponent.
     *
     * @param profileId the id of the {@link Profile} as determined by {@link Profile#getId()}
     * @param matchId   the id of the {@link Match}, specified by {@link Match#getId()}
     */
    void deleteMatch(String profileId, String matchId);

}
