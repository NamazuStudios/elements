package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.ApplicationProfileDao;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;
import com.namazustudios.socialengine.service.PSNApplicationProfileService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/24/17.
 */
public class SuperUserPSNApplicationProfileService implements PSNApplicationProfileService {

    private ApplicationProfileDao applicationProfileDao;

    @Override
    public PSNApplicationProfile getPSNApplicationProfile(String applicationNameOrId, String applicationProfileNameOrId) {
        return getApplicationProfileDao().getPSNApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public PSNApplicationProfile createApplicationProfile(String applicationNameOrId,
                                                          PSNApplicationProfile psnApplicationProfile) {
        return getApplicationProfileDao().createOrUpdateInactiveApplicationProfile(applicationNameOrId, psnApplicationProfile);
    }

    @Override
    public PSNApplicationProfile updateApplicationProfile(String applicationNameOrId,
                                                          String applicationProfileNameOrId,
                                                          PSNApplicationProfile psnApplicationProfile) {
        return getApplicationProfileDao().updateApplicationProfile(applicationNameOrId, applicationProfileNameOrId, psnApplicationProfile);
    }

    @Override
    public void deleteApplicationProfile(String applicationNameOrId, String applicationProfileNameOrId) {
        applicationProfileDao.softDeleteApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    public ApplicationProfileDao getApplicationProfileDao() {
        return applicationProfileDao;
    }

    @Inject
    public void setApplicationProfileDao(ApplicationProfileDao applicationProfileDao) {
        this.applicationProfileDao = applicationProfileDao;
    }

}
