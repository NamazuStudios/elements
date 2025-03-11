package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.mission.ScheduleEventNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.Schedule;
import dev.getelements.elements.sdk.model.mission.ScheduleEvent;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;
import java.util.Optional;

import static java.lang.System.currentTimeMillis;

/**
 * Manges {@link ScheduleEvent} instances within the database.
 */
@ElementServiceExport
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
     * Gets a {@link Pagination<ScheduleEvent>} of {@link ScheduleEvent} instances for the supplied {@link Schedule}
     *
     * @param scheduleNameOrId the name or id of the {@link Schedule}
     * @return a  {@link Pagination<ScheduleEvent>} of schedule events.
     */
    Pagination<ScheduleEvent> getScheduleEvents(String scheduleNameOrId, int offset, int count);

    /**
     * Gets a {@link Pagination<ScheduleEvent>} of {@link ScheduleEvent} instances for the supplied {@link Schedule}
     *
     * @param scheduleNameOrId the name or id of the {@link Schedule}
     * @param search           the search query
     * @return a  {@link Pagination<ScheduleEvent>} of schedule events.
     */
    Pagination<ScheduleEvent> getScheduleEvents(String scheduleNameOrId, int offset, int count, String search);

    /**
     * Gets all {@link ScheduleEvent} instances for the supplied {@link Schedule}
     *
     * @param scheduleNameOrId the name or id of the {@link ScheduleEvent}
     * @return a {@link List<ScheduleEvent>} of schedule events.
     */
    default List<ScheduleEvent> getAllScheduleEvents(final String scheduleNameOrId) {
        return getAllScheduleEvents(scheduleNameOrId, false, false);
    }

    /**
     * Gets all {@link ScheduleEvent} instances for the supplied {@link Schedule}
     *
     * @param scheduleNameOrId the name or id of the {@link ScheduleEvent}
     * @param includeExpired   true to include events that have expired
     * @param includeFuture    true to include events that have yet to be scheduled
     * @return a {@link List<ScheduleEvent>} of schedule events.
     */
    default List<ScheduleEvent> getAllScheduleEvents(final String scheduleNameOrId,
                                                     final boolean includeExpired, final boolean includeFuture) {
        final var reference = currentTimeMillis();
        return getAllScheduleEvents(scheduleNameOrId, includeExpired, includeFuture, reference);
    }

    /**
     * Gets all {@link ScheduleEvent} instances for the supplied {@link Schedule}
     *
     * @param scheduleNameOrId the name or id of the {@link ScheduleEvent}
     * @return a {@link List<ScheduleEvent>} of schedule events.
     */
    List<ScheduleEvent> getAllScheduleEvents(String scheduleNameOrId,
                                             boolean includeExpired, boolean includeFuture,
                                             long reference);

    /**
     * Finds a {@link Schedule} by name or ID.
     *
     * @param scheduleNameOrId the schedule name or id
     * @param scheduleEventId  the schedule event id
     * @return {@link Optional<ScheduleEvent>}
     */
    Optional<ScheduleEvent> findScheduleEventById(String scheduleNameOrId, String scheduleEventId);

    /**
     * Gets the {@link ScheduleEvent} with the id and schedule id.
     *
     * @param scheduleNameOrId the schedule name or id
     * @param scheduleEventId  the schedule event id
     * @return a {@link ScheduleEvent}, never null
     */
    default ScheduleEvent getScheduleEventById(String scheduleNameOrId, String scheduleEventId) {
        return findScheduleEventById(scheduleNameOrId, scheduleEventId)
                .orElseThrow(ScheduleEventNotFoundException::new);
    }

    /**
     * Deletes all events associated with the supplied schedule id.
     *
     * @param scheduleNameOrId the schedule name or id
     */
    void deleteScheduleEvents(String scheduleNameOrId);

    /**
     * Deletes a specific schedule event.
     *
     * @param scheduleNameOrId the schedule name or id
     * @param scheduleEventId  the schedule event id
     */
    void deleteScheduleEvent(String scheduleNameOrId, String scheduleEventId);

}
