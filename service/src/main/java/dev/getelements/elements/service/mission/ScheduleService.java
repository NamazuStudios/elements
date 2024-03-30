package dev.getelements.elements.service.mission;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.CreateScheduleRequest;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.model.mission.UpdateScheduleRequest;

public interface ScheduleService {
    Schedule createSchedule(CreateScheduleRequest createScheduleRequest);

    Schedule getScheduleByNameOrId(String scheduleNameOrId);

    Pagination<Schedule> getSchedules(int offset, int count);

    Pagination<Schedule> getSchedules(int offset, int count, String search);


    Schedule updateSchedule(UpdateScheduleRequest updatedSchedule);

    void deleteSchedule(String scheduleNameOrId);

}
