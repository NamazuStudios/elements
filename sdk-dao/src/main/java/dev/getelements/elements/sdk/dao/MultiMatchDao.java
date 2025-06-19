package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.MultiMatchNotFoundException;
import dev.getelements.elements.sdk.model.match.MultiMatch;
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
        value = MultiMatchDao.MULTI_MATCH_ADD_PROFILE,
        parameters = {MultiMatch.class, Profile.class},
        description = "Called when a profile is added to a multi match."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_REMOVE_PROFILE,
        parameters = {MultiMatch.class, Profile.class},
        description = "Called when a profile is removed from a multi match."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_UPDATED,
        parameters = MultiMatch.class,
        description = "Called when a multi-match was updated."
)
@ElementEventProducer(
        value = MultiMatchDao.MULTI_MATCH_DELETED,
        parameters = MultiMatch.class,
        description = "Called when a multi-match was deleted."
)
public interface MultiMatchDao {

    String MULTI_MATCH_CREATED = "dev.getelements.elements.sdk.model.dao.multi.match.created";

    String MULTI_MATCH_ADD_PROFILE = "dev.getelements.elements.sdk.model.dao.multi.match.add.profile";

    String MULTI_MATCH_REMOVE_PROFILE = "dev.getelements.elements.sdk.model.dao.multi.match.remove.profile";

    String MULTI_MATCH_UPDATED = "dev.getelements.elements.sdk.model.match.dao.multi.match.updated";

    String MULTI_MATCH_DELETED = "dev.getelements.elements.sdk.model.match.dao.multi.match.deleted";

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
     * Finds a {@link MultiMatch} by its ID.
     * @param multiMatchId the ID of the multi-match to find.
     *
     * @return the found {@link MultiMatch}, or an empty Optional if not found.
     */
    Optional<MultiMatch> findMultiMatch(String multiMatchId);

    /**
     * Gets a {@link MultiMatch} by its ID.
     *
     * @param multiMatchId the {@link MultiMatch}
     * @return the {@link MultiMatch}
     */
    default MultiMatch getMultiMatch(String multiMatchId) {
        return findMultiMatch(multiMatchId).orElseThrow(MultiMatchNotFoundException::new);
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
     * Adds a new {@link Profile} to the {@link MultiMatch}.
     *
     * @param multiMatchId the multi-match id receiving the profile
     * @param profile  the profile to add
     * @return the updated {@link MultiMatch}
     **/
    MultiMatch addProfile(String multiMatchId, Profile profile);

    /**
     * Removes the {@link Profile} to the {@link MultiMatch}.
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
     * @param configuration the {@link MatchmakingApplicationConfiguration} to use
     * @return the newly created {@link MultiMatch}
     */
    MultiMatch updateMultiMatch(MultiMatch configuration);

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
