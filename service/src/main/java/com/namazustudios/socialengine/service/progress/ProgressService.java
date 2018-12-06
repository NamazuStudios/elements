package com.namazustudios.socialengine.service.progress;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.mission.Progress;

public interface ProgressService {
    /**
     * Returns a list of {@link Progress} objects.
     *
     * @param offset the offset
     * @param count the count
     * @return the list of {@link Progress} instances
     */
    Pagination<Progress> getProgresses(int offset, int count);

    /**
     * Returns a list of {@link Progress} objects.
     *
     * @param offset the offset
     * @param count the count
     * @param query the search query
     * @return the list of {@link Progress} instances
     */
    Pagination<Progress> getProgresses(int offset, int count, String query);

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
     * @param progress the {@link Progress} to update
     * @return the {@link Progress} as it was written to the database
     */
    Progress updateProgress(Progress progress);

    /**
     * Creates a new {@link Progress}.  The ID of the progress, as specified by {@link Progress#getId()},
     * should be null and will be assigned.
     *
     * @param progress the {@link Progress} to create
     * @return the {@link Progress} as it was created by the service.
     */
    Progress createProgress(Progress progress);

    /**
     * Deletes the {@link Progress} with the supplied id or name.
     *
     * @param progressNameOrId the progress name or ID.
     */
    void deleteProgress(String progressNameOrId);

}
