package dev.getelements.elements.service.progress;

import dev.getelements.elements.dao.ProgressDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.Tabulation;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.mission.ProgressRow;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

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
    public Progress updateProgress(final Progress progress) {
        return getProgressDao().updateProgress(progress);
    }

    @Override
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
