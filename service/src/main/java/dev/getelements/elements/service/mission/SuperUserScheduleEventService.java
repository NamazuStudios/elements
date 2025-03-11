package dev.getelements.elements.service.mission;

import dev.getelements.elements.sdk.dao.MissionDao;
import dev.getelements.elements.sdk.dao.ScheduleDao;
import dev.getelements.elements.sdk.dao.ScheduleEventDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.CreateScheduleEventRequest;
import dev.getelements.elements.sdk.model.mission.ScheduleEvent;
import dev.getelements.elements.sdk.model.mission.UpdateScheduleEventRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.mission.ScheduleEventService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class SuperUserScheduleEventService implements ScheduleEventService {

    private ScheduleEventDao scheduleEventDao;

    private ValidationHelper validationHelper;

    private Provider<Transaction> transactionProvider;

    @Override
    public ScheduleEvent createScheduleEvent(
            final String scheduleNameOrId,
            final CreateScheduleEventRequest createScheduleEventRequest) {

        getValidationHelper().validateModel(createScheduleEventRequest);

        return getTransactionProvider().get().performAndClose(txn -> {

            final var missionDao = txn.getDao(MissionDao.class);
            final var scheduleDao = txn.getDao(ScheduleDao.class);
            final var scheduleEventDao = txn.getDao(ScheduleEventDao.class);

            final var schedule = scheduleDao.getScheduleByNameOrId(scheduleNameOrId);

            final var scheduleEvent = new ScheduleEvent();
            scheduleEvent.setSchedule(schedule);
            scheduleEvent.setBegin(createScheduleEventRequest.getBegin());
            scheduleEvent.setEnd(createScheduleEventRequest.getEnd());

            final var missionNamesOrIds = createScheduleEventRequest.getMissionNamesOrIds();
            final var missions = missionDao.getMissionsMatching(missionNamesOrIds);
            scheduleEvent.setMissions(missions);

            return scheduleEventDao.createScheduleEvent(scheduleEvent);

        });

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
        return getScheduleEventDao().getScheduleEventById(scheduleNameOrId, scheduleEventId);
    }

    @Override
    public ScheduleEvent updateScheduleEvent(
            final String scheduleNameOrId,
            final String scheduleEventNameOrId,
            final UpdateScheduleEventRequest updatedScheduleEvent) {

        getValidationHelper().validateModel(updatedScheduleEvent);

        return getTransactionProvider().get().performAndClose(txn -> {

            final var missionDao = txn.getDao(MissionDao.class);
            final var scheduleDao = txn.getDao(ScheduleDao.class);
            final var scheduleEventDao = txn.getDao(ScheduleEventDao.class);

            final var schedule = scheduleDao.getScheduleByNameOrId(scheduleNameOrId);

            final var scheduleEvent = getScheduleEventDao().getScheduleEventById(
                    scheduleNameOrId,
                    scheduleEventNameOrId);

            scheduleEvent.setSchedule(schedule);
            scheduleEvent.setBegin(updatedScheduleEvent.getBegin());
            scheduleEvent.setEnd(updatedScheduleEvent.getEnd());

            final var missionNamesOrIds = updatedScheduleEvent.getMissionNamesOrIds();
            final var missions = missionDao.getMissionsMatching(missionNamesOrIds);
            scheduleEvent.setMissions(missions);

            return scheduleEventDao.updateScheduleEvent(scheduleEvent);

        });

    }

    @Override
    public void deleteScheduleEvent(final String scheduleNameOrId, final String scheduleEventId) {
        getScheduleEventDao().deleteScheduleEvent(scheduleNameOrId, scheduleEventId);
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

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}
