
package dev.getelements.elements.service.mission;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.CreateScheduleEventRequest;
import dev.getelements.elements.model.mission.ScheduleEvent;
import dev.getelements.elements.model.mission.UpdateScheduleEventRequest;

public interface ScheduleEventService {

    ScheduleEvent createScheduleEvent(String scheduleNameOrId, CreateScheduleEventRequest createScheduleEventRequest);

    Pagination<ScheduleEvent> getScheduleEvents(String scheduleNameOrId, int offset, int count);
    Pagination<ScheduleEvent> getScheduleEvents(String scheduleNameOrId, int offset, int count, String search);

    ScheduleEvent getScheduleEventByNameOrId(
            String scheduleNameOrId,
            String scheduleEventId);

    ScheduleEvent updateScheduleEvent(String scheduleNameOrId,
                                      String scheduleEventNameOrId,
                                      UpdateScheduleEventRequest updatedScheduleEvent);

    void deleteScheduleEvent(String scheduleNameOrId, String scheduleEventId);

}
