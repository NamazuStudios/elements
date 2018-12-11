package com.namazustudios.socialengine.service.progress;

import com.namazustudios.socialengine.dao.ProgressDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.mission.Progress;
import com.namazustudios.socialengine.model.profile.Profile;

import javax.inject.Inject;
import java.util.function.Supplier;

public class UserProgressService implements ProgressService {

    protected Supplier<Profile> currentProfileSupplier;

    protected ProgressDao progressDao;

    @Override
    public Progress getProgress(String progressId) {
        Progress progress = progressDao.getProgress(progressId);

        if(!progress.getProfile().equals(currentProfileSupplier.get()))
            throw new NotFoundException();

        return progressDao.getProgress(progressId);
    }

    @Override
    public Pagination<Progress> getProgresses(int offset, int count) {
        return progressDao.getProgresses(currentProfileSupplier.get(), offset, count);
    }

    @Override
    public Pagination<Progress> getProgresses(int offset, int count, String query)  {
        return progressDao.getProgresses(currentProfileSupplier.get(), offset, count, query);
    }

    @Override
    public Progress updateProgress(Progress progress) { throw new ForbiddenException("Unprivileged requests are unable to modify progress."); }

    @Override
    public Progress createProgress(Progress progress) { throw new ForbiddenException("Unprivileged requests are unable to modify progress."); }

    @Override
    public void deleteProgress(String progressNameOrId) { throw new ForbiddenException("Unprivileged requests are unable to modify progress."); }


    public ProgressDao setProgressDao() {
        return progressDao;
    }

    @Inject
    public void getProgressDao(ProgressDao progressDao) {
        this.progressDao = progressDao;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

}
