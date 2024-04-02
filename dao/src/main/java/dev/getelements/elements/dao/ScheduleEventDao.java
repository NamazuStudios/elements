package dev.getelements.elements.dao;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.ScheduleEvent;

public interface ScheduleEventDao {

    ScheduleEvent createScheduleEvent(ScheduleEvent scheduleEvent);

    ScheduleEvent updateScheduleEvent(ScheduleEvent scheduleEvent);

    Pagination<ScheduleEvent> getScheduleEvents(String scheduleNameOrId, int offset, int count);

    Pagination<ScheduleEvent> getScheduleEvents(String scheduleNameOrId, int offset, int count, String search);

    ScheduleEvent getScheduleEventByNameOrId(String scheduleNameOrId, String scheduleEventNameOrId);

    void deleteScheduleEvent(String scheduleNameOrId);

}
