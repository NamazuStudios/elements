package dev.getelements.elements.dao;

import dev.getelements.elements.exception.mission.ScheduleEventNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.model.mission.ScheduleEvent;

import java.util.List;
import java.util.Optional;

/**
 * Manges {@link ScheduleEvent} instances within the database.
 */
public interface ScheduleEventDao {

    /**
     * Creates a new {@link ScheduleEvent}.
     *
     * @param scheduleEvent the {@link ScheduleEvent} to create
     * @return the object as written to the database
     */
    ScheduleEvent createScheduleEvent(ScheduleEvent scheduleEvent);

    /**
     * Updates the {@link ScheduleEvent}.
     *
     * @param scheduleEvent the {@link ScheduleEvent} to update
     * @return the object as written to the database
     */
    ScheduleEvent updateScheduleEvent(ScheduleEvent scheduleEvent);

    /**
     * Gets all {@link ScheduleEvent} instances for the supplied {@link Schedule}
     * @param scheduleNameOrId the name or id of the {@link ScheduleEvent}
     * @return a {@link List<ScheduleEvent>} of schedule events.
     */
    default List<ScheduleEvent> getAllScheduleEvents(String scheduleNameOrId) {
        return getAllScheduleEvents(scheduleNameOrId, false, false);
    }

    /**
     * Gets all {@link ScheduleEvent} instances for the supplied {@link Schedule}
     * @param scheduleNameOrId the name or id of the {@link ScheduleEvent}
     * @return a {@link List<ScheduleEvent>} of schedule events.
     */
    List<ScheduleEvent> getAllScheduleEvents(String scheduleNameOrId, boolean includeExpired, boolean includeFuture);

    /**
     * Gets a {@link Pagination<ScheduleEvent>} of {@link ScheduleEvent} instances for the supplied {@link Schedule}
     * @param scheduleNameOrId the name or id of the {@link Schedule}
     * @return a  {@link Pagination<ScheduleEvent>} of schedule events.
     */
    Pagination<ScheduleEvent> getScheduleEvents(String scheduleNameOrId, int offset, int count);

    /**
     * Gets a {@link Pagination<ScheduleEvent>} of {@link ScheduleEvent} instances for the supplied {@link Schedule}
     * @param scheduleNameOrId the name or id of the {@link Schedule}
     * @param search the search query
     * @return a  {@link Pagination<ScheduleEvent>} of schedule events.
     */
    Pagination<ScheduleEvent> getScheduleEvents(String scheduleNameOrId, int offset, int count, String search);

    /**
     * Finds a {@link Schedule} by name or ID.
     *
     * @param scheduleNameOrId the schedule name or id
     * @param scheduleEventId the schedule
     * @return
     */
    Optional<ScheduleEvent> findScheduleEventById(String scheduleNameOrId, String scheduleEventId);

    default ScheduleEvent getScheduleEventById(String scheduleNameOrId, String scheduleEventId) {
        return findScheduleEventById(scheduleNameOrId, scheduleEventId)
                .orElseThrow(ScheduleEventNotFoundException::new);
    }

    void deleteScheduleEvents(String scheduleNameOrId);

    void deleteScheduleEvent(String scheduleNameOrId, String scheduleEventId);

}
