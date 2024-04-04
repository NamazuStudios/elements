package dev.getelements.elements.service.mission;

import dev.getelements.elements.dao.ScheduleDao;
import dev.getelements.elements.dao.ScheduleEventDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.CreateScheduleRequest;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.model.mission.UpdateScheduleRequest;
import dev.getelements.elements.util.ValidationHelper;

import javax.inject.Inject;

public class SuperUserScheduleService implements ScheduleService {

    private ScheduleDao scheduleDao;

    private ScheduleEventDao scheduleEventDao;

    private ValidationHelper validationHelper;

    @Override
    public Schedule createSchedule(final CreateScheduleRequest createScheduleRequest) {

        getValidationHelper().validateModel(createScheduleRequest);

        final var schedule = new Schedule();
        schedule.setName(createScheduleRequest.getName());
        schedule.setDescription(createScheduleRequest.getDescription());

        return getScheduleDao().create(schedule);

    }

    @Override
    public Schedule getScheduleByNameOrId(final String scheduleNameOrId) {
        return getScheduleDao().getScheduleByNameOrId(scheduleNameOrId);
    }

    @Override
    public Pagination<Schedule> getSchedules(final int offset, final int count) {
        return getScheduleDao().getSchedules(offset, count);
    }

    @Override
    public Pagination<Schedule> getSchedules(final int offset, final int count, final String search) {
        return getScheduleDao().getSchedules(offset, count, search);
    }

    @Override
    public Schedule updateSchedule(final String scheduleNameOrId,
                                   final UpdateScheduleRequest updatedScheduleRequest) {

        getValidationHelper().validateModel(updatedScheduleRequest);

        final var schedule = getScheduleDao().getScheduleByNameOrId(scheduleNameOrId);
        schedule.setName(updatedScheduleRequest.getName());
        schedule.setDescription(updatedScheduleRequest.getDescription());

        return getScheduleDao().updateSchedule(schedule);

    }

    @Override
    public void deleteSchedule(final String scheduleNameOrId) {
        getScheduleDao().deleteSchedule(scheduleNameOrId);
        getScheduleEventDao().deleteScheduleEvents(scheduleNameOrId);
    }

    public ScheduleDao getScheduleDao() {
        return scheduleDao;
    }

    @Inject
    public void setScheduleDao(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public ScheduleEventDao getScheduleEventDao() {
        return scheduleEventDao;
    }

    @Inject
    public void setScheduleEventDao(ScheduleEventDao scheduleEventDao) {
        this.scheduleEventDao = scheduleEventDao;
    }

}
