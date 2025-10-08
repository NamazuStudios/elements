package dev.getelements.elements.sdk.service.match;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.match.MultiMatch;

@ElementPublic
@ElementServiceExport
public interface MultiMatchService {

    /**
     * Gets the {@link MultiMatch} with the specified id.
     *
     * @param matchId the Match ID as specified by {@link MultiMatch#getId()}
     *
     * @return the {@link MultiMatch}
     */
    MultiMatch getMatch(String matchId);

    /**
     * Gets all matches.
     *
     * @param offset the offset
     * @param count the count
     *
     * @return a {@link Pagination <MultiMatch>} instance containing the requested data
     */
    Pagination<MultiMatch> getMatches(int offset, int count);

    /**
     * Gets all matches, specifying search criteria.
     *
     * @param offset the offset
     * @param count the count
     *
     * @return a {@link Pagination<MultiMatch>} instance containing the requested data
     */
    Pagination<MultiMatch> getMatches(int offset, int count, String search);

    /**
     * Creates a {@link MultiMatch}.
     *
     * @param match the {@link MultiMatch} object
     * @return the {@link MultiMatch}, as it was written to the database
     */
    MultiMatch createMatch(MultiMatch match);

    /**
     * Updates a {@link MultiMatch}.
     *
     * @param match the {@link MultiMatch} object
     * @return the {@link MultiMatch}, as it was written to the database
     */
    MultiMatch updateMatch(String matchId, MultiMatch match);

    /**
     * Deletes a {@link MultiMatch} with the supplied ID, as determined by {@link MultiMatch#getId()}.
     *
     * @param matchId the match ID
     */
    void deleteMatch(String matchId);

    /**
     * Deletes all {@link MultiMatch} instances.
     */
    void deleteAllMatches();

}
