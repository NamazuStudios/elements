package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.MultiMatchNotFoundException;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.profile.Profile;

import java.util.List;
import java.util.Optional;

/**
 * Handles multi-matches, allowing multiple players to join or leave a match.
 */
public interface MultiMatchDao {

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
     * @param configuration the {@link MatchmakingApplicationConfiguration} to use
     * @return the newly created {@link MultiMatch}
     */
    MultiMatch createMultiMatch(MatchmakingApplicationConfiguration configuration);

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
