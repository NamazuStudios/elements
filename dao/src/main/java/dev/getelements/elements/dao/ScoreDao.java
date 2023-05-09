package dev.getelements.elements.dao;

import dev.getelements.elements.model.leaderboard.Leaderboard;
import dev.getelements.elements.model.leaderboard.Score;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Manipulates instances of {@link Score} within the database.
 *
 */
@Expose({
    @ModuleDefinition("namazu.elements.dao.score"),
    @ModuleDefinition(
        value = "namazu.socialengine.dao.score",
        deprecated = @DeprecationDefinition("Use namazu.elements.dao.score instead"))
})
public interface ScoreDao {

    /**
     * Creates an instance of {@link Score}, or updates the instance if the same leaderboard and {@link Profile}
     * are specified by {@link Score#getProfile()}.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param score the {@link Score} to specify
     * @return the {@link Score} as it was written to the database.
     */
    Score createOrUpdateScore(String leaderboardNameOrId, Score score);

}
