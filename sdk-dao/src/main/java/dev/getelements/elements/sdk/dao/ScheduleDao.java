package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.mission.ScheduleNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.Schedule;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Optional;

/**
 * Provides access to {@link Schedule} instances within the database.
 */
@ElementServiceExport
@ElementEventProducer(
        value = ScheduleDao.SCHEDULE_CREATED,
        parameters = Schedule.class,
        description = "Called when a schedule was created."
)
@ElementEventProducer(
        value = ScheduleDao.SCHEDULE_CREATED,
        parameters = {Schedule.class, Transaction.class},
        description = "Called when a schedule was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = ScheduleDao.SCHEDULE_UPDATED,
        parameters = Schedule.class,
        description = "Called when a schedule was updated."
)
@ElementEventProducer(
        value = ScheduleDao.SCHEDULE_UPDATED,
        parameters = {Schedule.class, Transaction.class},
        description = "Called when a schedule was updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = ScheduleDao.SCHEDULE_DELETED,
        parameters = Schedule.class,
        description = "Called when a schedule was deleted."
)
@ElementEventProducer(
        value = ScheduleDao.SCHEDULE_DELETED,
        parameters = {Schedule.class, Transaction.class},
        description = "Called when a schedule was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface ScheduleDao {

    String SCHEDULE_CREATED = "dev.getelements.elements.sdk.model.dao.schedule.created";

    String SCHEDULE_UPDATED = "dev.getelements.elements.sdk.model.dao.schedule.updated";

    String SCHEDULE_DELETED = "dev.getelements.elements.sdk.model.dao.schedule.deleted";

    /**
     * Creates a new {@link Schedule} in the database.
     *
     * @param schedule the schedule
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
