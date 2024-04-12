package dev.getelements.elements.service.mission;

import dev.getelements.elements.dao.ScheduleEventDao;
import dev.getelements.elements.dao.ScheduleProgressDao;
import dev.getelements.elements.dao.Transaction;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.profile.Profile;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

public class UserScheduleProgressService implements ScheduleProgressService {

    private Supplier<Profile> profileSupplier;

    private ScheduleProgressDao  scheduleProgressDao;

    private Provider<Transaction> transactionProvider;

    @Override
    public Pagination<Progress> getScheduleProgressService(
            final String scheduleNameOrId,
            final int offset, final int count) {

        final var profileId = getProfileSupplier().get().getId();

        getTransactionProvider()
                .get()
                .performAndCloseV(txn -> syncProgresses(txn, profileId, scheduleNameOrId));

        return getScheduleProgressDao()
                .getProgresses(profileId, scheduleNameOrId, offset, count);

    }

    private void syncProgresses(final Transaction txn,
                                final String profileId,
                                final String scheduleNameOrId) {

        final var scheduleEventDao = txn.getDao(ScheduleEventDao.class);
        final var scheduleProgressDao = txn.getDao(ScheduleProgressDao.class);

        final var missions = scheduleEventDao
                .getAllScheduleEvents(scheduleNameOrId)
                .stream()
                .flatMap(event -> event.getMissions().stream())
                .collect(toList());

        scheduleProgressDao.createProgressesForMissionsIn(scheduleNameOrId, profileId, missions);
        scheduleProgressDao.deleteProgressesForMissionsNotIn(scheduleNameOrId, profileId, missions);

    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

    public ScheduleProgressDao getScheduleProgressDao() {
        return scheduleProgressDao;
    }

    @Inject
    public void setScheduleProgressDao(ScheduleProgressDao scheduleProgressDao) {
        this.scheduleProgressDao = scheduleProgressDao;
    }

    public Supplier<Profile> getProfileSupplier() {
        return profileSupplier;
    }

    @Inject
    public void setProfileSupplier(Supplier<Profile> profileSupplier) {
        this.profileSupplier = profileSupplier;
    }
}
