package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.IosApplicationConfigurationDao;
import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import com.namazustudios.socialengine.service.IosApplicationConfigurationService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class SuperUserIosApplicationConfigurationService implements IosApplicationConfigurationService {

    private IosApplicationConfigurationDao iosApplicationConfigurationDao;

    @Override
    public void deleteApplicationProfile(final String applicationNameOrId,
                                         final String applicationProfileNameOrId) {
        getIosApplicationConfigurationDao().softDeleteApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public IosApplicationConfiguration getApplicationProfile(final String applicationNameOrId,
                                                             final String applicationProfileNameOrId) {
        return getIosApplicationConfigurationDao().getIosApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public IosApplicationConfiguration createApplicationProfile(final String applicationNameOrId,
                                                                final IosApplicationConfiguration iosApplicationProfile) {
        return getIosApplicationConfigurationDao().createOrUpdateInactiveApplicationProfile(applicationNameOrId, iosApplicationProfile);
    }

    @Override
    public IosApplicationConfiguration updateApplicationProfile(final String applicationNameOrId,
                                                                final String applicationProfileNameOrId,
                                                                final IosApplicationConfiguration iosApplicationProfile) {
        return getIosApplicationConfigurationDao().updateApplicationProfile(applicationNameOrId, applicationProfileNameOrId, iosApplicationProfile);
    }

    public IosApplicationConfigurationDao getIosApplicationConfigurationDao() {
        return iosApplicationConfigurationDao;
    }

    @Inject
    public void setIosApplicationConfigurationDao(IosApplicationConfigurationDao iosApplicationConfigurationDao) {
        this.iosApplicationConfigurationDao = iosApplicationConfigurationDao;
    }

}
