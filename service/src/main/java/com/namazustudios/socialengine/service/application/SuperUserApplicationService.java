package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;

/**
 * {@link ApplicationService} implemented for whent he current user has {@link User.Level#SUPERUSER} access.
 *
 * Created by patricktwohig on 7/10/15.
 */
public class SuperUserApplicationService implements ApplicationService {

    @Override
    public Application createApplication(Application application) {
        return null;
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count) {
        return null;
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count, String search) {
        return null;
    }

    @Override
    public Application getApplication(String nameOrId) {
        return null;
    }

    @Override
    public Application updateApplication(String nameOrId, Application application) {
        return null;
    }

    @Override
    public void deleteApplication(String nameOrId) {

    }

}
