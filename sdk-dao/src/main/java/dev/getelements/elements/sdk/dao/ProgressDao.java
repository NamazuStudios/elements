package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.mission.ProgressNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.Tabulation;
import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.model.mission.Progress;
import dev.getelements.elements.sdk.model.mission.ProgressRow;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;
import java.util.Optional;

/**
 * Created by davidjbrooks on 12/05/18.
 */

@ElementServiceExport
@ElementEventProducer(
        value = ProgressDao.PROGRESS_CREATED,
        parameters = Progress.class,
        description = "Called when a progress was created."
)
@ElementEventProducer(
        value = ProgressDao.PROGRESS_CREATED,
        parameters = {Progress.class, Transaction.class},
        description = "Called when a progress was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = ProgressDao.PROGRESS_UPDATED,
        parameters = Progress.class,
        description = "Called when a progress was updated."
)
@ElementEventProducer(
        value = ProgressDao.PROGRESS_UPDATED,
        parameters = {Progress.class, Transaction.class},
        description = "Called when a progress was updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = ProgressDao.PROGRESS_DELETED,
        parameters = Progress.class,
        description = "Called when a progress was deleted."
)
@ElementEventProducer(
        value = ProgressDao.PROGRESS_DELETED,
        parameters = {Progress.class, Transaction.class},
        description = "Called when a progress was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface ProgressDao {

    String PROGRESS_CREATED = "dev.getelements.elements.sdk.model.dao.progress.created";

    String PROGRESS_UPDATED = "dev.getelements.elements.sdk.model.dao.progress.updated";

    String PROGRESS_DELETED = "dev.getelements.elements.sdk.model.dao.progress.deleted";

    /**
     * Gets progresses specifying the user, offset and the count.
     *
     * @param offset the offset
     * @param count  the count
     * @param tags
     * @param profile {@link Profile}
     * @return a {@link Pagination} of {@link Progress} objects.
     * @param profile {@link Profile}
     */
    Pagination<Progress> getProgresses(Profile profile, int offset, int count, List<String> tags);

    /**
     * Gets progresses specifying the user, offset and the count, specifying a search filter.
     *
     * @param offset the offset
     * @param count  the count
     * @param tags
     * @param search a query to filter the results
     * @param profile {@link Profile}
     * @return a {@link Pagination} of {@link Progress} objects.
     */
    Pagination<Progress> getProgresses(Profile profile, int offset, int count, List<String> tags, String search);

    /**
     * Gets progresses specifying the offset and the count.
     *
     * @param offset the offset
     * @param count  the count
     * @param tags the tags
     * @return a {@link Pagination} of {@link Progress} objects.
     */
    Pagination<Progress> getProgresses(int offset, int count, List<String> tags);

    /**
     * Gets progresses specifying the offset and the count, specifying a search filter.
     *
     * @param offset the offset
     * @param count  the count
     * @param tags the tags
     * @param search a query to filter the results
     * @return a {@link Pagination} of {@link Progress} objects.
     */
    Pagination<Progress> getProgresses(int offset, int count, List<String> tags, String search);

    /**
     * Gets Progress instances in a tabular fashion.
     *
     * @return a {@link Tabulation} of {@link ProgressRow} instances
     */
    Tabulation<ProgressRow> getProgressesTabular();

    /**
     * Gets the progress with the id, or throws a {@link NotFoundException} if the
     * progress can't be found.
     *
     * @return the {@link Progress} that was requested, never null
     * @throws ProgressNotFoundException if no {@link Progress} matches
     */
    default Progress getProgress(String progressId) {
        return findProgress(progressId).orElseThrow(ProgressNotFoundException::new);
    }

    /**
     * Gets the progress with the id, or throws a {@link NotFoundException} if the
     * progress can't be found.
     *
     * @return the {@link Progress} that was requested, never null
     * @throws ProgressNotFoundException if no {@link Progress} matches
     */
    Optional<Progress> findProgress(String progressId);

    /**
     * Updates the progress, or throws a {@link NotFoundException} if the
     * progress can't be found.  The {@link Progress#getId()} is used to key the progress being updated.
     *
     * @return the {@link Progress} as it was written into the database
     * @throws InvalidDataException if the state of the passed in Progress is invalid
     */
    Progress updateProgress(Progress progress);

    /**
     * Creates a progress.  The value of {@link Progress#getId()} will be ignored.
     *
     * @return the {@link Progress} as it was written into the database
     * @throws InvalidDataException if the state of the passed in Progress is invalid
     * @throws DuplicateException   if the passed in Progress has a name that already exists
     */
    Progress createOrGetExistingProgress(Progress progress);

    /**
     * Deletes a progress.
     *
     * @param progressId the progress ID
     */
    void deleteProgress(String progressId);

    /**
     * Advances {@link Progress} by the specified number of actions.
     *
     * @param progress         the {@link Progress}
     * @param actionsPerformed the amount of actions to apply
     * @return
     */
    Progress advanceProgress(Progress progress, int actionsPerformed);

    /**
     * Gets the {@link Progress} instance for the supplied {@link Profile} and {@link Mission} instance.
     *
     * @param profile         the {@link Profile} linked to the progress
     * @param missionNameOrId the {@link Mission} linked to the progress
     * @return the {@link Progress} instances
     * @throws ProgressNotFoundException if the process cannot be found
     */
    default Progress getProgressForProfileAndMission(final Profile profile, final String missionNameOrId) {
        return findProgressForProfileAndMission(profile, missionNameOrId).orElseThrow(ProgressNotFoundException::new);
    }

    /**
     * Gets the {@link Progress} instance for the supplied {@link Profile} and {@link Mission} instance.
     *
     * @param profile         the {@link Profile} linked to the progress
     * @param missionNameOrId the {@link Mission} linked to the progress
     * @return the {@link Progress} instances
     */
    Optional<Progress> findProgressForProfileAndMission(Profile profile, String missionNameOrId);
}