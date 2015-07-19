package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.ApplicationProfileDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;
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

    @Override
    public ApplicationProfile getApplicationProfile(String applicationNameOrId,
                                                    String applicationProfileNameOrId) {
        return applicationProfileDao.getApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public <T extends ApplicationProfile> T getApplicationProfile(String applicationNameOrId,
                                                                  String applicationProfileNameOrId,
                                                                  Class<T> type) {
        return applicationProfileDao.getApplicationProfile(applicationNameOrId, applicationProfileNameOrId, type);
    }

    @Override
    public PSNApplicationProfile createApplicationProfile(String applicationNameOrId,
                                                          PSNApplicationProfile psnApplicationProfile) {
        return applicationProfileDao.createOrUpdateInactiveApplicationProfile(applicationNameOrId,
                psnApplicationProfile);
    }

    @Override
    public PSNApplicationProfile updateApplicationProfile(String applicationNameOrId,
                                                          String applicationProfileNameOrId,
                                                          PSNApplicationProfile psnApplicationProfile) {
        return applicationProfileDao.updateApplicationProfile(applicationNameOrId,
                applicationProfileNameOrId, psnApplicationProfile);
    }

    @Override
    public void deleteApplicationProfile(String applicationNameOrId,
                                         String applicationProfileNameOrId,
                                         Class<? extends ApplicationProfile> type) {
        applicationProfileDao.softDeleteApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

}
