package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;
import dev.getelements.elements.sdk.model.leaderboard.Score;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Manipulates instances of {@link Score} within the database.
 */

@ElementServiceExport
public interface ScoreDao {

    /**
     * Creates an instance of {@link Score}, or updates the instance if the same leaderboard and {@link Profile}
     * are specified by {@link Score#getProfile()}.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param score               the {@link Score} to specify
     * @return the {@link Score} as it was written to the database.
     */
    Score createOrUpdateScore(String leaderboardNameOrId, Score score);

}
