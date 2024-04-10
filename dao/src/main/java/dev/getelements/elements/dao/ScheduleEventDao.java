package dev.getelements.elements.dao;

import dev.getelements.elements.exception.mission.ScheduleEventNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.ScheduleEvent;

import java.util.Optional;

public interface ScheduleEventDao {

    ScheduleEvent createScheduleEvent(ScheduleEvent scheduleEvent);

    ScheduleEvent updateScheduleEvent(ScheduleEvent scheduleEvent);

    Pagination<ScheduleEvent> getScheduleEvents(String scheduleNameOrId, int offset, int count);

    Pagination<ScheduleEvent> getScheduleEvents(String scheduleNameOrId, int offset, int count, String search);

    Optional<ScheduleEvent> findScheduleEventById(String scheduleNameOrId, String scheduleEventId);

    default ScheduleEvent getScheduleEventById(String scheduleNameOrId, String scheduleEventId) {
        return findScheduleEventById(scheduleNameOrId, scheduleEventId)
                .orElseThrow(ScheduleEventNotFoundException::new);
    }

    void deleteScheduleEvents(String scheduleNameOrId);

    void deleteScheduleEvent(String scheduleNameOrId, String scheduleEventId);


}
