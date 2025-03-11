package dev.getelements.elements.sdk.service.mission;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.Progress;
import dev.getelements.elements.sdk.model.mission.Schedule;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Manages {@link Progress} instances in relation to a {@link Schedule}.
 */
@ElementPublic
@ElementServiceExport
public interface ScheduleProgressService {

    /**
     * Gets all {@link Progress} instances that are associated with the {@link Schedule} based on the current time.
     *
     * @param scheduleNameOrId the {@link Schedule} name or id
     * @param offset the offset
     * @param count the count
     * @return the {@link Pagination<Schedule>} instance
     */
    Pagination<Progress> getScheduleProgressService(String scheduleNameOrId, int offset, int count);

}
