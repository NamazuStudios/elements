package dev.getelements.elements.dao.mongo.mission;

import dev.getelements.elements.dao.ScheduleDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Schedule;

public class MongoScheduleDao implements ScheduleDao {
    
    @Override
    public Schedule create(final Schedule schedule) {
        return null;
    }

    @Override
    public Schedule getScheduleByNameOrId(final String scheduleNameOrId) {
        return null;
    }

    @Override
    public Pagination<Schedule> getSchedules(final int offset, final int count) {
        return null;
    }

    @Override
    public Pagination<Schedule> getSchedules(final int offset, final int count, final String search) {
        return null;
    }

    @Override
    public Schedule updateSchedule(final Schedule updatedSchedule) {
        return null;
    }

    @Override
    public void deleteSchedule(final String scheduleNameOrId) {

    }

}
