package dev.getelements.elements.dao;

import dev.getelements.elements.exception.mission.ScheduleNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Schedule;

import java.util.Optional;

/**
 * Provides access to {@link Schedule} instances within the database.
 */
public interface ScheduleDao {

    /**
     * Creates a new {@link Schedule} in the database.
     *
     * @param schedule the schedule
     *
     * @return the {@link Schedule} as created
     */
    Schedule create(Schedule schedule);

    /**
     * Finds the {@link Schedule} with the supplied name or id.
     *
     * @param scheduleNameOrId the schedule name and id
     * @return an {@link Optional<Schedule>}
     */
    Optional<Schedule> findScheduleByNameOrId(String scheduleNameOrId);

    /**
     * Gets the {@link Schedule} with the supplied name or id.
     *
     * @param scheduleNameOrId
     * @param scheduleNameOrId the schedule name and id
     * @return the {@link Schedule}, never null
     * @throws ScheduleNotFoundException
     */
    default Schedule getScheduleByNameOrId(String scheduleNameOrId) {
        return findScheduleByNameOrId(scheduleNameOrId).orElseThrow(ScheduleNotFoundException::new);
    }

    Pagination<Schedule> getSchedules(int offset, int count);

    Pagination<Schedule> getSchedules(int offset, int count, String search);

    /**
     * Updates a {@link Schedule} with the supplied {@link Schedule}.
     *
     * @param updatedSchedule the updated {@link Schedule}
     * @return the {@link Schedule} as updated
     */
    Schedule updateSchedule(Schedule updatedSchedule);

    void deleteSchedule(String scheduleNameOrId);

}
