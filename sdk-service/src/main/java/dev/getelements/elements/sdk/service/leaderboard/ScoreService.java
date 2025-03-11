package dev.getelements.elements.sdk.service.leaderboard;

import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;
import dev.getelements.elements.sdk.model.leaderboard.Score;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Creates and manages instances of {@link Score}.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
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
