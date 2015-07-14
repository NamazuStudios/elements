package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;

import javax.inject.Inject;

/**
 * {@link ApplicationService} implemented for when the current user has {@link User.Level#SUPERUSER} access.
 *
 * Created by patricktwohig on 7/10/15.
 */
public class SuperUserApplicationService implements ApplicationService {

    @Inject
    private ApplicationDao applicationDao;

    @Override
    public Application createApplication(Application application) {
        return applicationDao.createOrUpdateInactiveApplication(application);
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count) {
        return applicationDao.getActiveApplications(offset, count);
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count, String search) {
        return applicationDao.getActiveApplications(offset, count, search);
    }

    @Override
    public Application getApplication(String nameOrId) {
        return applicationDao.getActiveApplication(nameOrId);
    }

    @Override
    public Application updateApplication(String nameOrId, Application application) {
        return applicationDao.updateActiveApplication(nameOrId, application);
    }

    @Override
    public void deleteApplication(String nameOrId) {
        applicationDao.softDeleteApplication(nameOrId);
    }

}
