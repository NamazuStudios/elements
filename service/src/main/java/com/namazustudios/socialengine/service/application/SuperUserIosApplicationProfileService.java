package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.IosApplicationProfileDao;
import com.namazustudios.socialengine.model.application.IosApplicationProfile;
import com.namazustudios.socialengine.service.IosApplicationProfileService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class SuperUserIosApplicationProfileService implements IosApplicationProfileService {

    private IosApplicationProfileDao iosApplicationProfileDao;

    @Override
    public void deleteApplicationProfile(final String applicationNameOrId,
                                         final String applicationProfileNameOrId) {
        getIosApplicationProfileDao().softDeleteApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public IosApplicationProfile getApplicationProfile(final String applicationNameOrId,
                                                       final String applicationProfileNameOrId) {
        return getIosApplicationProfileDao().getIosApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public IosApplicationProfile createApplicationProfile(final String applicationNameOrId,
                                                          final IosApplicationProfile iosApplicationProfile) {
        return getIosApplicationProfileDao().createOrUpdateInactiveApplicationProfile(applicationNameOrId, iosApplicationProfile);
    }

    @Override
    public IosApplicationProfile updateApplicationProfile(final String applicationNameOrId,
                                                          final String applicationProfileNameOrId,
                                                          final IosApplicationProfile iosApplicationProfile) {
        return getIosApplicationProfileDao().updateApplicationProfile(applicationNameOrId, applicationProfileNameOrId, iosApplicationProfile);
    }

    public IosApplicationProfileDao getIosApplicationProfileDao() {
        return iosApplicationProfileDao;
    }

    @Inject
    public void setIosApplicationProfileDao(IosApplicationProfileDao iosApplicationProfileDao) {
        this.iosApplicationProfileDao = iosApplicationProfileDao;
    }

}
