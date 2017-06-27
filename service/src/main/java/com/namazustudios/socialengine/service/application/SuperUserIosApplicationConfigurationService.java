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
    public void deleteApplicationConfiguration(final String applicationNameOrId,
                                               final String applicationProfileNameOrId) {
        getIosApplicationConfigurationDao().softDeleteApplicationConfiguration(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public IosApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                   final String applicationProfileNameOrId) {
        return getIosApplicationConfigurationDao().getIosApplicationConfiguration(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public IosApplicationConfiguration createApplicationConfiguration(final String applicationNameOrId,
                                                                      final IosApplicationConfiguration iosApplicationConfiguration) {
        return getIosApplicationConfigurationDao().createOrUpdateInactiveApplicationConfiguration(applicationNameOrId, iosApplicationConfiguration);
    }

    @Override
    public IosApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                                      final String applicationConfigurationNameOrId,
                                                                      final IosApplicationConfiguration iosApplicationConfiguration) {
        return getIosApplicationConfigurationDao().updateApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId, iosApplicationConfiguration);
    }

    public IosApplicationConfigurationDao getIosApplicationConfigurationDao() {
        return iosApplicationConfigurationDao;
    }

    @Inject
    public void setIosApplicationConfigurationDao(IosApplicationConfigurationDao iosApplicationConfigurationDao) {
        this.iosApplicationConfigurationDao = iosApplicationConfigurationDao;
    }

}
