package dev.getelements.elements.service.progress;

import dev.getelements.elements.sdk.dao.ProgressDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.Tabulation;
import dev.getelements.elements.sdk.model.mission.CreateProgressRequest;
import dev.getelements.elements.sdk.model.mission.Progress;
import dev.getelements.elements.sdk.model.mission.ProgressRow;

import dev.getelements.elements.sdk.model.mission.UpdateProgressRequest;
import dev.getelements.elements.sdk.service.progress.ProgressService;
import jakarta.inject.Inject;
import java.util.List;

public class SuperUserProgressService implements ProgressService {

    private ProgressDao progressDao;

    @Override
    public Progress getProgress(final String progressId) {
        return getProgressDao().getProgress(progressId);
    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count, final List<String> tags) {
        return getProgressDao().getProgresses(offset, count, tags);
    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count,
                                              final List<String> tags, final String query) {
        return getProgressDao().getProgresses(offset, count, tags, query);
    }

    @Override
    public Tabulation<ProgressRow> getProgressesTabular() {
        return getProgressDao().getProgressesTabular();
    }

    @Override
    public Progress updateProgress(final String progressId, final UpdateProgressRequest request) {

        final var progress = new Progress();
        progress.setId(progressId);
        progress.setRemaining(request.getRemaining());
        progress.setCurrentStep(request.getCurrentStep());

        return getProgressDao().updateProgress(progress);
    }

    @Override
    @Deprecated
    public Progress updateProgress(final Progress progress) {
        return getProgressDao().updateProgress(progress);
    }

    @Override
    public Progress createProgress(final CreateProgressRequest request) {

        final var progress = new Progress();

        progress.setProfile(progress.getProfile());
        progress.setMission(request.getMission());

        return getProgressDao().createOrGetExistingProgress(progress);
    }

    @Override
    @Deprecated
    public Progress createProgress(final Progress progress) {
        return getProgressDao().createOrGetExistingProgress(progress);
    }

    @Override
    public void deleteProgress(final String progressNameOrId) {
        getProgressDao().deleteProgress(progressNameOrId);
    }

    public ProgressDao getProgressDao() {
        return progressDao;
    }

    @Inject
    public void setProgressDao(ProgressDao progressDao) {
        this.progressDao = progressDao;
    }

}
