package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;
import dev.getelements.elements.sdk.model.leaderboard.Score;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Manipulates instances of {@link Score} within the database.
 */

@ElementServiceExport
@ElementEventProducer(
        value = ScoreDao.SCORE_CREATED_OR_UPDATED,
        parameters = Score.class,
        description = "Called when a score was created or updated."
)
@ElementEventProducer(
        value = ScoreDao.SCORE_CREATED_OR_UPDATED,
        parameters = {Score.class, Transaction.class},
        description = "Called when a score was created or updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface ScoreDao {

    String SCORE_CREATED_OR_UPDATED = "dev.getelements.elements.sdk.model.dao.score.created.or.updated";

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
