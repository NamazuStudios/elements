package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.model.leaderboard.Score;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

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
