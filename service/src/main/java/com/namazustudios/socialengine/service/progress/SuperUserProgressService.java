package com.namazustudios.socialengine.service.progress;

import com.namazustudios.socialengine.dao.ProgressDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.mission.Progress;

import javax.inject.Inject;
import java.util.Set;

public class SuperUserProgressService implements ProgressService {

    private ProgressDao progressDao;

    @Override
    public Progress getProgress(final String progressId) {
        return getProgressDao().getProgress(progressId);
    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count, final Set<String> tags) {
        return getProgressDao().getProgresses(offset, count, tags);
    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count,
                                              final Set<String> tags, final String query) {
        return getProgressDao().getProgresses(offset, count, tags, query);
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
