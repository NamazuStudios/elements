package dev.getelements.elements.service.mission;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.mission.Schedule;

/**
 * Manages {@link Progress} instances in relation to a {@link Schedule}.
 */
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
