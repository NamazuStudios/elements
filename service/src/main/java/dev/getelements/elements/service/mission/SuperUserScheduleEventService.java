package dev.getelements.elements.service.mission;

import dev.getelements.elements.dao.MissionDao;
import dev.getelements.elements.dao.ScheduleDao;
import dev.getelements.elements.dao.ScheduleEventDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.CreateScheduleEventRequest;
import dev.getelements.elements.model.mission.ScheduleEvent;
import dev.getelements.elements.model.mission.UpdateScheduleEventRequest;
import dev.getelements.elements.util.ValidationHelper;

import javax.inject.Inject;

public class SuperUserScheduleEventService implements ScheduleEventService {

    private MissionDao missionDao;

    private ScheduleDao scheduleDao;

    private ScheduleEventDao scheduleEventDao;

    private ValidationHelper validationHelper;

    @Override
    public ScheduleEvent createScheduleEvent(
            final String scheduleNameOrId,
            final CreateScheduleEventRequest createScheduleEventRequest) {

        getValidationHelper().validateModel(createScheduleEventRequest);

        final var schedule = getScheduleDao().getScheduleByNameOrId(scheduleNameOrId);

        final var scheduleEvent = new ScheduleEvent();

        scheduleEvent.setSchedule(schedule);
        scheduleEvent.setBegin(createScheduleEventRequest.getBegin());
        scheduleEvent.setEnd(createScheduleEventRequest.getEnd());

        final var missionNamesOrIds = createScheduleEventRequest.getMissionNamesOrIds();
        final var missions = getMissionDao().getMissionsMatching(missionNamesOrIds);
        scheduleEvent.setMissions(missions);

        return getScheduleEventDao().createScheduleEvent(scheduleEvent);

    }

    @Override
    public Pagination<ScheduleEvent> getScheduleEvents(
            final String scheduleNameOrId,
            final int offset, final int count) {
        return getScheduleEventDao().getScheduleEvents(scheduleNameOrId, offset, count);
    }

    @Override
    public Pagination<ScheduleEvent> getScheduleEvents(
            final String scheduleNameOrId,
            final int offset, final int count, final String search) {
        return getScheduleEventDao().getScheduleEvents(scheduleNameOrId, offset, count, search);
    }

    @Override
    public ScheduleEvent getScheduleEventByNameOrId(
            final String scheduleNameOrId,
            final String scheduleEventId) {
        return getScheduleEventDao().getScheduleEventByNameOrId(scheduleNameOrId, scheduleEventId);
    }

    @Override
    public ScheduleEvent updateScheduleEvent(
            final String scheduleNameOrId,
            final String scheduleEventNameOrId,
            final UpdateScheduleEventRequest updatedScheduleEvent) {

        getValidationHelper().validateModel(updatedScheduleEvent);

        final var schedule = getScheduleDao().getScheduleByNameOrId(scheduleNameOrId);

        final var scheduleEvent = getScheduleEventDao().getScheduleEventByNameOrId(
                scheduleNameOrId,
                scheduleEventNameOrId)
        ;

        scheduleEvent.setSchedule(schedule);
        scheduleEvent.setBegin(updatedScheduleEvent.getBegin());
        scheduleEvent.setEnd(updatedScheduleEvent.getEnd());

        final var missionNamesOrIds = updatedScheduleEvent.getMissionNamesOrIds();
        final var missions = getMissionDao().getMissionsMatching(missionNamesOrIds);
        scheduleEvent.setMissions(missions);

        return getScheduleEventDao().createScheduleEvent(scheduleEvent);

    }

    @Override
    public void deleteScheduleEvent(final String scheduleNameOrId, final String scheduleEventId) {
        getScheduleEventDao().deleteScheduleEvent(scheduleNameOrId, scheduleEventId);
    }

    public ScheduleDao getScheduleDao() {
        return scheduleDao;
    }

    @Inject
    public void setScheduleDao(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

    public MissionDao getMissionDao() {
        return missionDao;
    }

    @Inject
    public void setMissionDao(MissionDao missionDao) {
        this.missionDao = missionDao;
    }

    public ScheduleEventDao getScheduleEventDao() {
        return scheduleEventDao;
    }

    @Inject
    public void setScheduleEventDao(ScheduleEventDao scheduleEventDao) {
        this.scheduleEventDao = scheduleEventDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
