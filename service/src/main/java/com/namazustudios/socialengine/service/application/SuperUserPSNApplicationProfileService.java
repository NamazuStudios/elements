package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.PSNApplicationProfileDao;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;
import com.namazustudios.socialengine.service.PSNApplicationProfileService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/24/17.
 */
public class SuperUserPSNApplicationProfileService implements PSNApplicationProfileService {

    private PSNApplicationProfileDao psnApplicationProfileDao;

    @Override
    public PSNApplicationProfile getPSNApplicationProfile(String applicationNameOrId, String applicationProfileNameOrId) {
        return getPsnApplicationProfileDao().getPSNApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public PSNApplicationProfile createApplicationProfile(String applicationNameOrId,
                                                          PSNApplicationProfile psnApplicationProfile) {
        return getPsnApplicationProfileDao().createOrUpdateInactiveApplicationProfile(applicationNameOrId, psnApplicationProfile);
    }

    @Override
    public PSNApplicationProfile updateApplicationProfile(String applicationNameOrId,
                                                          String applicationProfileNameOrId,
                                                          PSNApplicationProfile psnApplicationProfile) {
        return getPsnApplicationProfileDao().updateApplicationProfile(applicationNameOrId, applicationProfileNameOrId, psnApplicationProfile);
    }

    @Override
    public void deleteApplicationProfile(String applicationNameOrId, String applicationProfileNameOrId) {
        getPsnApplicationProfileDao().softDeleteApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    public PSNApplicationProfileDao getPsnApplicationProfileDao() {
        return psnApplicationProfileDao;
    }

    @Inject
    public void setPsnApplicationProfileDao(PSNApplicationProfileDao psnApplicationProfileDao) {
        this.psnApplicationProfileDao = psnApplicationProfileDao;
    }

}
