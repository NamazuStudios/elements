
package dev.getelements.elements.sdk.service.mission;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.CreateScheduleEventRequest;
import dev.getelements.elements.sdk.model.mission.ScheduleEvent;
import dev.getelements.elements.sdk.model.mission.UpdateScheduleEventRequest;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
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
