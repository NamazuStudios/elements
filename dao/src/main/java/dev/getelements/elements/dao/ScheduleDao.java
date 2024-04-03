package dev.getelements.elements.dao;

import dev.getelements.elements.exception.mission.ScheduleNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.model.mission.UpdateScheduleRequest;

import java.util.Optional;

public interface ScheduleDao {

    Schedule create(Schedule schedule);

    Optional<Schedule> findSchedulByNameOrId(String scheduleNameOrId);

    default Schedule getScheduleByNameOrId(String scheduleNameOrId) {
        return findSchedulByNameOrId(scheduleNameOrId).orElseThrow(ScheduleNotFoundException::new);
    }

    Pagination<Schedule> getSchedules(int offset, int count);

    Pagination<Schedule> getSchedules(int offset, int count, String search);

    Schedule updateSchedule(Schedule updatedSchedule);

    void deleteSchedule(String scheduleNameOrId);

}
