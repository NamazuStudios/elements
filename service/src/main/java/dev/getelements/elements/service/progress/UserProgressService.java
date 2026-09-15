package dev.getelements.elements.service.progress;

import dev.getelements.elements.sdk.dao.MissionDao;
import dev.getelements.elements.sdk.dao.ProgressDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.Tabulation;
import dev.getelements.elements.sdk.model.mission.CreateProgressRequest;
import dev.getelements.elements.sdk.model.mission.Progress;
import dev.getelements.elements.sdk.model.mission.ProgressRow;
import dev.getelements.elements.sdk.model.mission.UpdateProgressRequest;
import dev.getelements.elements.sdk.model.profile.Profile;

import dev.getelements.elements.sdk.service.progress.ProgressService;
import jakarta.inject.Inject;
import java.util.List;
import java.util.function.Supplier;

public class UserProgressService implements ProgressService {

    private Supplier<Profile> currentProfileSupplier;

    private ProgressDao progressDao;

    private MissionDao missionDao;

    @Override
    public Progress getProgress(final String progressId) {

        final Progress progress = getProgressDao().getProgress(progressId);

        if(!progress.getProfile().equals(getCurrentProfileSupplier().get())) {
            throw new NotFoundException();
        }

        return getProgressDao().getProgress(progressId);
    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count, final List<String> tags) {
        return getProgressDao().getProgresses(getCurrentProfileSupplier().get(), offset, count, tags);
    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count, final List<String> tags, final String query)  {
        return getProgressDao().getProgresses(getCurrentProfileSupplier().get(), offset, count, tags, query);
    }

    @Override
    public Tabulation<ProgressRow> getProgressesTabular() {
        throw new ForbiddenException();
    }

    @Override
    public Progress updateProgress(String progressId, UpdateProgressRequest request) {
        throw new ForbiddenException("Unprivileged requests are unable to modify progress.");
    }

    @Override
    public Progress createProgress(CreateProgressRequest progress) {
        throw new ForbiddenException("Unprivileged requests are unable to modify progress.");
    }

    @Override
    @Deprecated
    public Progress updateProgress(Progress progress) { throw new ForbiddenException("Unprivileged requests are unable to modify progress."); }

    @Override
    @Deprecated
    public Progress createProgress(Progress progress) { throw new ForbiddenException("Unprivileged requests are unable to modify progress."); }

    @Override
    public void deleteProgress(String progressNameOrId) { throw new ForbiddenException("Unprivileged requests are unable to modify progress."); }

    @Override
    public Progress advanceProgress(final String progressId, final int actions) {

        final var progress = getProgressDao().getProgress(progressId);

        if (!progress.getProfile().getId().equals(getCurrentProfileSupplier().get().getId())) {
            throw new NotFoundException();
        }

        final var mission = getMissionDao().getMissionByNameOrId(progress.getMission().getId());

        if (mission.getAuthoritative() == null || mission.getAuthoritative()) {
            throw new ForbiddenException(
                    "This mission's progress is authoritative and can only be advanced by server-side Element code.");
        }

        return getProgressDao().advanceProgress(progress, actions);

    }

    public ProgressDao getProgressDao() {
        return progressDao;
    }

    @Inject
    public void setProgressDao(ProgressDao progressDao) {
        this.progressDao = progressDao;
    }

    public MissionDao getMissionDao() {
        return missionDao;
    }

    @Inject
    public void setMissionDao(MissionDao missionDao) {
        this.missionDao = missionDao;
    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

}
