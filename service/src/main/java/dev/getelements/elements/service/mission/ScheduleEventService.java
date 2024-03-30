
package dev.getelements.elements.service.mission;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.CreateScheduleEventRequest;
import dev.getelements.elements.model.mission.ScheduleEvent;
import dev.getelements.elements.model.mission.UpdateScheduleEventRequest;

public interface ScheduleEventService {
    ScheduleEvent createScheduleEvent(CreateScheduleEventRequest createScheduleEventRequest);

    Pagination<ScheduleEvent> getScheduleEvents(int offset, int count);
    Pagination<ScheduleEvent> getScheduleEvents(int offset, int count, String search);

    ScheduleEvent getScheduleEventByNameOrId(String scheduleNameOrId);

    ScheduleEvent updateScheduleEvent(UpdateScheduleEventRequest updatedScheduleEvent);

    void deleteScheduleEvent(String scheduleNameOrId);

}
