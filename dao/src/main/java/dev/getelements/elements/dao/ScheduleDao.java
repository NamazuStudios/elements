package dev.getelements.elements.dao;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.model.mission.UpdateScheduleRequest;

public interface ScheduleDao {

    Schedule create(Schedule schedule);

    Schedule getScheduleByNameOrId(String scheduleNameOrId);

    Pagination<Schedule> getSchedules(int offset, int count);

    Pagination<Schedule> getSchedules(int offset, int count, String search);

    Schedule updateSchedule(Schedule updatedSchedule);

    void deleteSchedule(String scheduleNameOrId);

}
