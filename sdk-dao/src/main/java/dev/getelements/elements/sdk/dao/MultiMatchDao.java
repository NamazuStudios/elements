package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.dao.UniqueCodeDao.GenerationParameters;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.MultiMatchNotFoundException;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.match.MultiMatchStatus;
import dev.getelements.elements.sdk.model.profile.Profile;

import java.util.List;
import java.util.Optional;

/**
 * Handles multi-matches, allowing multiple players to join or leave a match.
 */
@ElementServiceExport
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_CREATED,
        parameters = MultiMatch.class,
        description = "Called when a multi-match was created."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_CREATED,
        parameters = {MultiMatch.class, Transaction.class},
        description = "Called when a multi-match was created."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_ADD_PROFILE,
        parameters = {MultiMatch.class, Profile.class},
        description = "Called when a profile is added to a multi match."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_ADD_PROFILE,
        parameters = {MultiMatch.class, Profile.class, Transaction.class},
        description = "Called when a profile is added to a multi match."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_REMOVE_PROFILE,
        parameters = {MultiMatch.class, Profile.class},
        description = "Called when a profile is removed from a multi match."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_REMOVE_PROFILE,
        parameters = {MultiMatch.class, Profile.class, Transaction.class},
        description = "Called when a profile is removed from a multi match."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_UPDATED,
        parameters = MultiMatch.class,
        description = "Called when a multi-match was updated."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_UPDATED,
        parameters = {MultiMatch.class, Transaction.class},
        description = "Called when a multi-match was updated."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_EXPIRED,
        parameters = MultiMatch.class,
        description = "Called when a multi-match was expired."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_EXPIRED,
        parameters = {MultiMatch.class, Transaction.class},
        description = "Called when a multi-match was expired."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_DELETED,
        parameters = MultiMatch.class,
        description = "Called when a multi-match was deleted."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_DELETED,
        parameters = {MultiMatch.class, Transaction.class},
        description = "Called when a multi-match was deleted."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCHES_TRUNCATED,
        parameters = MultiMatch.class,
        description = "Called when all multi-matches were deleted. Will not drive individual deletion events."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCHES_TRUNCATED,
        parameters = {MultiMatch.class, Transaction.class},
        description = "Called when all multi-matches were deleted. Will not drive individual deletion events."
)
public interface MultiMatchDao {

    String MULTI_MATCH_CREATED = "dev.getelements.elements.sdk.model.dao.multi.match.created";

    String MULTI_MATCH_ADD_PROFILE = "dev.getelements.elements.sdk.model.dao.multi.match.add.profile";

    String MULTI_MATCH_REMOVE_PROFILE = "dev.getelements.elements.sdk.model.dao.multi.match.remove.profile";

    String MULTI_MATCH_UPDATED = "dev.getelements.elements.sdk.model.match.dao.multi.match.updated";

    String MULTI_MATCH_EXPIRED = "dev.getelements.elements.sdk.model.match.dao.multi.match.expired";

    String MULTI_MATCH_DELETED = "dev.getelements.elements.sdk.model.match.dao.multi.match.deleted";

    String MULTI_MATCHES_TRUNCATED = "dev.getelements.elements.sdk.model.match.dao.multi.matches.truncated";

    /**
     * Gets all {@link MultiMatch} instances.
     *
     * @return a {@link List} of {@link MultiMatch} instances
     */
    default List<MultiMatch> getAllMultiMatches() {
        return getAllMultiMatches("");
    }

    /**
     * Gets all {@link MultiMatch} instances.
     *
     * @param query the query to execute
     * @return a {@link List} of {@link MultiMatch} instances
     */
    List<MultiMatch> getAllMultiMatches(String query);

    /**
     * Fetches a paginated subset of {@link MultiMatch} instances matching the search.
     * @param offset - Pagination offset
     * @param count - Maximum objects in results
     * @param search - Search query to filter results
     * @return Pagination of {@link MultiMatch}
     */
    Pagination<MultiMatch> getMultiMatches(int offset, int count, String search);

    /**
     * Fetches a paginated subset of {@link MultiMatch} instances.
     * @param offset - Pagination offset
     * @param count - Maximum objects in results
     * @return Pagination of {@link MultiMatch}
     */
    default Pagination<MultiMatch> getMultiMatches(int offset, int count) {
        return getMultiMatches(offset, count, "");
    }

    /**
     * Finds a {@link MultiMatch} by its ID.
     * @param multiMatchId the ID of the multi-match to find.
     *
     * @return the found {@link MultiMatch}, or an empty Optional if not found.
     */
    Optional<MultiMatch> findMultiMatch(String multiMatchId);

    /**
     * Finds a {@link MultiMatch} by its ID.
     * @param joinCode the join code of the multi match.
     *
     * @return the found {@link MultiMatch}, or an empty Optional if not found.
     */
    Optional<MultiMatch> findMultiMatchByJoinCode(String joinCode);

    /**
     * Finds the latest {@link MultiMatch} for the given configuration and profile ID. This method will exclude any
     * matches that the specific profile currently not participating in. If no match meets the criteria, then an empty
     * optional is returned allowing downstream code to create a new match.
     *
     * Additionally, a query can be provided to further filter the matches that are considered. The query is appended
     * to the supplied criteria.
     *
     * @param configuration the matchmaking configuration
     * @param profileId the profile ID to find the latest match for
     * @param query the query to execute when searching for a match
     * @return the found {@link MultiMatch}, or an empty Optional if not found.
     */
    Optional<MultiMatch> findOldestAvailableMultiMatchCandidate(
            MatchmakingApplicationConfiguration configuration,
            String profileId,
            String query
    );

    /**
     * Gets a {@link MultiMatch} by its ID.
     *
     * @param multiMatchId the {@link MultiMatch}'s id
     * @return the {@link MultiMatch}
     */
    default MultiMatch getMultiMatch(final String multiMatchId) {
        return findMultiMatch(multiMatchId).orElseThrow(MultiMatchNotFoundException::new);
    }

    /**
     * Gets a {@link MultiMatch} by its ID.
     *
     * @param joinCode the {@link MultiMatch}'s join code
     * @return the {@link MultiMatch}
     */
    default MultiMatch getMultiMatchByJoinCode(final String joinCode) {
        return findMultiMatchByJoinCode(joinCode).orElseThrow(MultiMatchNotFoundException::new);
    }

    /**
     * Gets all {@link Profile} instances in the {@link MultiMatch}.
     *
     * @param multiMatchId tbe multi-match id to get profiles from
     * @return the {@link List} of {@link Profile} instances in the {@link MultiMatch}
     * @throws MultiMatchNotFoundException if no {@link MultiMatch} with the given ID exists
     */
    List<Profile> getProfiles(String multiMatchId);

    /**
     * Adds a new {@link Profile} to the {@link MultiMatch}. Automatically sets the match status to
     * {@link MultiMatchStatus#FULL} if the match reaches capacity.
     *
     * @param multiMatchId the multi-match id receiving the profile
     * @param profile  the profile to add
     * @return the updated {@link MultiMatch}
     **/
    MultiMatch addProfile(String multiMatchId, Profile profile);

    /**
     * Removes the {@link Profile} to the {@link MultiMatch}. Automatically sets the match status to
     * {@link MultiMatchStatus#OPEN} if the match was previously full and not {@link MultiMatchStatus#CLOSED}.
     *
     * @param multiMatchId the multi-match id receiving the profile
     * @param profile  the profile to add
     * @return the updated {@link MultiMatch}
     **/
    MultiMatch removeProfile(String multiMatchId, Profile profile);

    /**
     * Creates a new {@link MultiMatch} with no players.
     *
     * @param multiMatch the {@link MultiMatch} to create
     * @return the newly created {@link MultiMatch}
     */
    MultiMatch createMultiMatch(MultiMatch multiMatch);

    /**
     * Creates a new {@link MultiMatch} with no players.
     *
     * @param multiMatch the {@link MultiMatch} to create
     * @param joinCodeGenerationParameters the {@link GenerationParameters} to use when generating the join code
     * @return the newly created {@link MultiMatch}
     */
    MultiMatch createMultiMatch(MultiMatch multiMatch, GenerationParameters joinCodeGenerationParameters);

    /**
     * Creates a new {@link MultiMatch} with no players.
     *
     * @param configuration the {@link MatchmakingApplicationConfiguration} to use
     * @return the newly created {@link MultiMatch}
     */
    MultiMatch updateMultiMatch(String matchId, MultiMatch configuration);

    /**
     * Flags the {@link MultiMatch} as open, allowing players to join. If the match is currently full, then
     * this operation will set the match as {@link MultiMatchStatus#FULL}, otherwise it will set the match to
     * {@link MultiMatchStatus#OPEN}.
     *
     * @param multiMatchId the {@link MultiMatch}
     * @return the updated {@link MultiMatch}
     */
    MultiMatch openMatch(String multiMatchId);

    /**
     * Flags the {@link MultiMatch} as closed, disallowing players to join. This operation will set the match as
     * {@link MultiMatchStatus#CLOSED}. Fails if the match is {@link MultiMatchStatus#ENDED}.
     * @param multiMatchId the multi-match id to refresh
     * @return the updated {@link MultiMatch}
     */
    MultiMatch closeMatch(String multiMatchId);

    /**
     * Flags the {@link MultiMatch} as ended, disallowing players to join. This operation will set the match as
     * {@link MultiMatchStatus#ENDED}. Fails if the match is already {@link MultiMatchStatus#ENDED}.
     *
     * @param multiMatchId the multi-match id to refresh
     * @return the updated {@link MultiMatch}
     *
     */
    MultiMatch endMatch(String multiMatchId);

    /**
     * Refreshes the {@link MultiMatch}, resetting its expiry timer.
     *
     * @param multiMatchId the multi-match id to refresh
     * @return the updated {@link MultiMatch}
     */
    MultiMatch refreshMatch(String multiMatchId);

    /**
     * Deletes all of the {@link MultiMatch} instances.
     */
    void deleteAllMultiMatches();

    /**
     * Deletes the {@link MultiMatch} with the given ID, throwing an exception if it does not exist.
     */
    default void deleteMultiMatch(final String multiMatchId) {
        if (!tryDeleteMultiMatch(multiMatchId)) {
            throw new MultiMatchNotFoundException();
        }
    }

    /**
     * Deletes the {@link MultiMatch} with the given ID, returning true if it was deleted, false if it did not exist.
     */
    boolean tryDeleteMultiMatch(String multiMatchId);

}
