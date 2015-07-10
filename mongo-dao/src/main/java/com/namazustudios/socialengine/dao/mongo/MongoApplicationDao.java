package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;

/**
 * Created by patricktwohig on 7/10/15.
 */
public class MongoApplicationDao implements ApplicationDao {

    @Override
    public Application createOrUpdateInactiveApplication(Application application) {
        return null;
    }

    @Override
    public Pagination<Application> getActiveApplications(int offset, int count) {
        return null;
    }

    @Override
    public Pagination<Application> getActiveApplications(int offset, int count, String search) {
        return null;
    }

    @Override
    public Application getActiveApplication(String nameOrId) {
        return null;
    }

    @Override
    public Application updateActiveApplication(String nameOrId, Application application) {
        return null;
    }

    @Override
    public void softDeleteApplication(String nameOrId) {

    }

}
