package com.namazustudios.socialengine.service.progress;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.mission.Progress;

public class SuperUserProgressService extends UserProgressService implements ProgressService {

    @Override
    public Pagination<Progress> getProgresses(int offset, int count) { return progressDao.getProgresses(offset, count); }

    @Override
    public Pagination<Progress> getProgresses(int offset, int count, String query) { return progressDao.getProgresses(offset, count, query); }

    @Override
    public Progress updateProgress(Progress progress) { return progressDao.updateProgress(progress); }

    @Override
    public Progress createProgress(Progress progress) { return progressDao.createProgress(progress); }

    @Override
    public void deleteProgress(String progressNameOrId) { progressDao.deleteProgress(progressNameOrId); }

}
