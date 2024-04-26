package dev.getelements.elements.service.mission;

import dev.getelements.elements.dao.ScheduleDao;
import dev.getelements.elements.dao.ScheduleEventDao;
import dev.getelements.elements.dao.Transaction;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.CreateScheduleRequest;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.model.mission.UpdateScheduleRequest;
import dev.getelements.elements.util.ValidationHelper;

import javax.inject.Inject;
import javax.inject.Provider;

public class SuperUserScheduleService implements ScheduleService {

    private ScheduleDao scheduleDao;

    private ScheduleEventDao scheduleEventDao;

    private ValidationHelper validationHelper;

    private Provider<Transaction> transactionProvider;

    @Override
    public Schedule createSchedule(final CreateScheduleRequest createScheduleRequest) {

        getValidationHelper().validateModel(createScheduleRequest);

        final var schedule = new Schedule();
        schedule.setName(createScheduleRequest.getName());
        schedule.setDescription(createScheduleRequest.getDescription());
        schedule.setDisplayName(createScheduleRequest.getDisplayName());

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

        return getTransactionProvider()
                .get()
                .performAndClose(txn -> {
                    final var scheduleDao = txn.getDao(ScheduleDao.class);
                    final var schedule = scheduleDao.getScheduleByNameOrId(scheduleNameOrId);
                    schedule.setName(updatedScheduleRequest.getName());
                    schedule.setDisplayName(updatedScheduleRequest.getDisplayName());
                    schedule.setDescription(updatedScheduleRequest.getDescription());
                    return scheduleDao.updateSchedule(schedule);
                });

    }

    @Override
    public void deleteSchedule(final String scheduleNameOrId) {
        getTransactionProvider()
                .get()
                .performAndCloseV(txn -> {
                    final var scheduleDao = txn.getDao(ScheduleDao.class);
                    final var scheduleEventDao = txn.getDao(ScheduleEventDao.class);
                    scheduleEventDao.deleteScheduleEvents(scheduleNameOrId);
                    scheduleDao.deleteSchedule(scheduleNameOrId);
                });
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

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}
