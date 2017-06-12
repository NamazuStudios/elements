package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.ApplicationProfileDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.service.ApplicationProfileService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class SuperUserApplicationProfileService implements ApplicationProfileService {

    @Inject
    private ApplicationProfileDao applicationProfileDao;

    @Override
    public Pagination<ApplicationProfile> getApplicationProfiles(String applicationNameOrId, int offset, int count) {
        return applicationProfileDao.getActiveApplicationProfiles(applicationNameOrId, offset, count);
    }

    @Override
    public Pagination<ApplicationProfile> getApplicationProfiles(String applicationNameOrId,
                                                                 int offset, int count, String search) {
        return applicationProfileDao.getActiveApplicationProfiles(applicationNameOrId, offset, count, search);
    }

}
