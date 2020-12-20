package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.model.leaderboard.Score;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Creates and manages instances of {@link Score}.
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.scoped.score"),
    @ExposedModuleDefinition(value = "namazu.elements.service.unscoped.score", annotation = Unscoped.class)
})
public interface ScoreService {

    /**
     * Creates, or updates, a new {@link Score}.  If the profile submitted with the the {@link Score} matches an
     * existing {@link Leaderboard} and {@link Profile}, then the existing record will be updated.
     *
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param score the {@link Score}
     * @return the {@link Score} as it was written to the database
     */
    Score createOrUpdateScore(String leaderboardNameOrId, Score score);

}
