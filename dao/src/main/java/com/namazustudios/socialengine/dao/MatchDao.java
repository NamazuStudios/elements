package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import com.namazustudios.socialengine.model.profile.Profile;

/**
 * Created by patricktwohig on 7/20/17.
 */
@Expose(module = "namazu.socialengine.dao.match")
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
     * Crates an instance of {@link Match} in the database.  Note that this does not
     *
     * @param match the {@link Match} to create
     * @return a {@link Match} as it was written to the database
     */
    Match createMatch(Match match);

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
     * Delets a {@link Match}, specyifying the id of the {@link Match}.  Note that a {@link Match} may not be deleted
     * if it is already matched to an opponent.
     *
     * @param profileId the id of the {@link Profile} as determined by {@link Profile#getId()}
     * @param matchId the id of the {@link Match}, specified by {@link Match#getId()}
     */
    void deleteMatch(String profileId, String matchId);

}
