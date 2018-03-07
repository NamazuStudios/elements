package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;

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
