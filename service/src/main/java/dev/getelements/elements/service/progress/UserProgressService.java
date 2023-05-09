package dev.getelements.elements.service.progress;

import dev.getelements.elements.dao.ProgressDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.profile.Profile;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class UserProgressService implements ProgressService {

    private Supplier<Profile> currentProfileSupplier;

    private ProgressDao progressDao;

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
    public Progress updateProgress(Progress progress) { throw new ForbiddenException("Unprivileged requests are unable to modify progress."); }

    @Override
    public Progress createProgress(Progress progress) { throw new ForbiddenException("Unprivileged requests are unable to modify progress."); }

    @Override
    public void deleteProgress(String progressNameOrId) { throw new ForbiddenException("Unprivileged requests are unable to modify progress."); }

    public ProgressDao getProgressDao() {
        return progressDao;
    }

    @Inject
    public void setProgressDao(ProgressDao progressDao) {
        this.progressDao = progressDao;
    }

    public ProgressDao setProgressDao() {
        return progressDao;
    }

    @Inject
    public void getProgressDao(ProgressDao progressDao) {
        this.progressDao = progressDao;
    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

}
