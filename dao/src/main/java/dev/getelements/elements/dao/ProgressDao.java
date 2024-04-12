package dev.getelements.elements.dao;

import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Mission;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.util.List;

/**
 * Created by davidjbrooks on 12/05/18.
 */
@Expose({
        @ModuleDefinition("eci.elements.dao.progress"),
        @ModuleDefinition(
                value = "namazu.elements.dao.progress",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.progress instead")
        ),
        @ModuleDefinition(
                value = "namazu.socialengine.dao.progress",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.progress instead")
        )
})
public interface ProgressDao {

    /**
     * Gets progresses specifying the user, offset and the count.
     *
     * @Profile the {@link Profile}
     * @param offset the offset
     * @param count the count
     * @param tags
     * @return a {@link Pagination} of {@link Progress} objects.
     */
    Pagination<Progress> getProgresses(Profile profile, int offset, int count, List<String> tags);

    /**
     * Gets progresses specifying the user, offset and the count, specifying a search filter.
     *
     * @Profile the {@link Profile}
     * @param offset the offset
     * @param count the count
     * @param tags
     * @param search a query to filter the results
     * @return a {@link Pagination} of {@link Progress} objects.
     */
    Pagination<Progress> getProgresses(Profile profile, int offset, int count, List<String> tags, String search);

    /**
     * Gets progresses specifying the offset and the count.
            *
            * @param offset the offset
     * @param count the count
     * @param tags
     * @return a {@link Pagination} of {@link Progress} objects.
     */
    Pagination<Progress> getProgresses(int offset, int count, List<String> tags);

    /**
     * Gets progresses specifying the offset and the count, specifying a search filter.
     *
     * @param offset the offset
     * @param count the count
     * @param tags
     * @param search a query to filter the results
     * @return a {@link Pagination} of {@link Progress} objects.
     */
    Pagination<Progress> getProgresses(int offset, int count, List<String> tags, String search);

    /**
     * Gets the progress with the id, or throws a {@link NotFoundException} if the
     * progress can't be found.
     *
     * @return the {@link Progress} that was requested, never null
     */
    Progress getProgress(String progressId);

    /**
     * Updates the progress, or throws a {@link NotFoundException} if the
     * progress can't be found.  The {@link Progress#getId()} is used to key the progress being updated.
     *
     * @return the {@link Progress} as it was written into the database
     * @throws InvalidDataException
     *     if the state of the passed in Progress is invalid
     */
    Progress updateProgress(Progress progress);

    /**
     * Creates a progress.  The value of {@link Progress#getId()} will be ignored.
     *
     * @return the {@link Progress} as it was written into the database
     * @throws InvalidDataException
     *     if the state of the passed in Progress is invalid
     * @throws DuplicateException
     *     if the passed in Progress has a name that already exists
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
     * @param progress the {@link Progress}
     * @param actionsPerformed the amount of actions to apply
     * @return
     */
    Progress advanceProgress(Progress progress, int actionsPerformed);

    /**
     * Gets all active {@link Progress} instances for the supplied {@link Profile} and {@link Mission} instance.
     *
     * @param profile the {@link Profile} linked to the progress
     * @param missionNameOrId the {@link Mission} linked to the progress
     * @return all active {@link Progress} instances
     */
    Progress getProgressForProfileAndMission(Profile profile, String missionNameOrId);

}
