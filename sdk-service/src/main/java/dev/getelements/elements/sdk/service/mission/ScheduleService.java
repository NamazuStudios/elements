package dev.getelements.elements.sdk.service.mission;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.CreateScheduleRequest;
import dev.getelements.elements.sdk.model.mission.Schedule;
import dev.getelements.elements.sdk.model.mission.UpdateScheduleRequest;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ScheduleService {

    Schedule createSchedule(CreateScheduleRequest createScheduleRequest);

    Schedule getScheduleByNameOrId(String scheduleNameOrId);

    Pagination<Schedule> getSchedules(int offset, int count);

    Pagination<Schedule> getSchedules(int offset, int count, String search);


    Schedule updateSchedule(String scheduleNameOrId, UpdateScheduleRequest updatedSchedule);

    void deleteSchedule(String scheduleNameOrId);

}
