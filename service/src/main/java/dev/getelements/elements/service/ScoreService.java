package dev.getelements.elements.service;

import dev.getelements.elements.model.leaderboard.Leaderboard;
import dev.getelements.elements.model.leaderboard.Score;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Creates and manages instances of {@link Score}.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.score"
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.score",
                deprecated = @DeprecationDefinition("Use eci.elements.service.score instead.")
        )
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
