package dev.getelements.elements.sdk.service.progress;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.Tabulation;
import dev.getelements.elements.sdk.model.mission.CreateProgressRequest;
import dev.getelements.elements.sdk.model.mission.Progress;
import dev.getelements.elements.sdk.model.mission.ProgressRow;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.mission.UpdateProgressRequest;

import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ProgressService {
    /**
     * Returns a list of {@link Progress} objects.
     *
     * @param offset the offset
     * @param count the count
     * @param tags
     * @return the list of {@link Progress} instances
     */
    Pagination<Progress> getProgresses(int offset, int count, List<String> tags);

    /**
     * Returns a list of {@link Progress} objects.
     *
     * @param offset the offset
     * @param count the count
     * @param tags
     * @param query the search query
     * @return the list of {@link Progress} instances
     */
    Pagination<Progress> getProgresses(int offset, int count, List<String> tags, String query);

    /**
     * Gets a {@link Tabulation<ProgressRow>} complete with the tabular data for that row.
     *
     * @return the progress in a tabular fashion.
     */
    Tabulation<ProgressRow> getProgressesTabular();

    /**
     * Gets a progress with specified name or ID.
     *
     * @param progressId the UserId
     *
     * @return the progress
     */
    Progress getProgress(String progressId);

    /**
     * Updates the {@link Progress}. The {@link Progress#getId()} method is
     * used to key the {@link Progress}.
     *
     * @param progressId the database id of the progress object
     * @param request the changeable parameters to overwrite in {@link UpdateProgressRequest}
     * @return the {@link Progress} as it was written to the database
     */
    Progress updateProgress(String progressId, UpdateProgressRequest request);

    /**
     * Updates the {@link Progress}. The {@link Progress#getId()} method is
     * used to key the {@link Progress}.
     *
     * @param progress the {@link Progress} to update
     * @return the {@link Progress} as it was written to the database
     */
    @Deprecated
    Progress updateProgress(Progress progress);

    /**
     * Creates a new {@link Progress}.  The ID of the progress, as specified by {@link Progress#getId()},
     * should be null and will be assigned.
     *
     * @param progress the parameters used to create a new {@link Progress}
     * @return the {@link Progress} as it was created by the service.
     */
    Progress createProgress(CreateProgressRequest progress);

    /**
     * Creates a new {@link Progress}.  The ID of the progress, as specified by {@link Progress#getId()},
     * should be null and will be assigned.
     *
     * @param progress the {@link Progress} to create
     * @return the {@link Progress} as it was created by the service.
     */
    @Deprecated
    Progress createProgress(Progress progress);

    /**
     * Deletes the {@link Progress} with the supplied id or name.
     *
     * @param progressNameOrId the progress name or ID.
     */
    void deleteProgress(String progressNameOrId);

}
