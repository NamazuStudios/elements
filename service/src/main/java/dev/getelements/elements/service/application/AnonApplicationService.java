package dev.getelements.elements.service.application;

import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.service.ApplicationService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class AnonApplicationService implements ApplicationService {

    private ApplicationDao applicationDao;

    private ApplicationUrls applicationUrls;

    @Override
    public Application createApplication(Application application) {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<Application> getApplications() {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count) {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count, String search) {
        throw new ForbiddenException();
    }

    @Override
    public Application getApplication(String nameOrId) {
        final Application application = getApplicationDao().getActiveApplication(nameOrId);
        return getApplicationUrls().addPublicUrls(application);
    }

    @Override
    public Application updateApplication(String nameOrId, Application application) {
        throw new ForbiddenException();
    }

    @Override
    public void deleteApplication(String nameOrId) {
        throw new ForbiddenException();
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public ApplicationUrls getApplicationUrls() {
        return applicationUrls;
    }

    @Inject
    public void setApplicationUrls(ApplicationUrls applicationUrls) {
        this.applicationUrls = applicationUrls;
    }

}
