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
     * @param query the query to execute
     * @return a {@link List} of {@link MultiMatch} instances
     */
    List<MultiMatch> getMultiMatches(String query);

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
     * Adds a new {@link Profile} to the {@link MultiMatch}.
     * @param multiMatchId the multi-match id receiving the profile
     * @param profile  the profile to add
     * @return the updated {@link MultiMatch}
     **/
    MultiMatch addProfile(String multiMatchId, Profile profile);

    /**
     * Deletes the {@link Profile} to the {@link MultiMatch}.
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
    MultiMatch updateMultimatch(MatchmakingApplicationConfiguration configuration);

}
