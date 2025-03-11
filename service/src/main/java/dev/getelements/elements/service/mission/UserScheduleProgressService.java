package dev.getelements.elements.service.mission;

import dev.getelements.elements.sdk.dao.ScheduleEventDao;
import dev.getelements.elements.sdk.dao.ScheduleProgressDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.Progress;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.service.mission.ScheduleProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.function.Supplier;

public class UserScheduleProgressService implements ScheduleProgressService {

    private static final Logger logger = LoggerFactory.getLogger(UserScheduleProgressService.class);

    private Supplier<Profile> profileSupplier;

    private Provider<Transaction> transactionProvider;

    private ScheduleProgressDao scheduleProgressDao;

    @Override
    public Pagination<Progress> getScheduleProgressService(
            final String scheduleNameOrId,
            final int offset, final int count) {

        final var profileId = getProfileSupplier().get().getId();

        return getTransactionProvider()
                .get()
                .performAndClose(txn -> syncProgresses(txn, profileId, scheduleNameOrId, offset, count));

    }

    private Pagination<Progress> syncProgresses(final Transaction txn,
                                final String profileId,
                                final String scheduleNameOrId,
                                final int offset, final int count) {
        final var scheduleEventDao = txn.getDao(ScheduleEventDao.class);
        final var scheduleProgressDao = txn.getDao(ScheduleProgressDao.class);

        final var events = scheduleEventDao.getAllScheduleEvents(scheduleNameOrId);

        final var assigned = scheduleProgressDao.assignProgressesForMissionsIn(
                scheduleNameOrId,
                profileId,
                events);

        final var unassigned = scheduleProgressDao.unassignProgressesForMissionsNotIn(
                scheduleNameOrId,
                profileId,
                events);

        logger.debug("Assigned progresses {}", assigned);
        logger.debug("Unassigned progresses {}", unassigned);

        return scheduleProgressDao.getProgresses(profileId, scheduleNameOrId, offset, count);

    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

    public Supplier<Profile> getProfileSupplier() {
        return profileSupplier;
    }

    @Inject
    public void setProfileSupplier(Supplier<Profile> profileSupplier) {
        this.profileSupplier = profileSupplier;
    }

    public ScheduleProgressDao getScheduleProgressDao() {
        return scheduleProgressDao;
    }

    @Inject
    public void setScheduleProgressDao(ScheduleProgressDao scheduleProgressDao) {
        this.scheduleProgressDao = scheduleProgressDao;
    }

}
