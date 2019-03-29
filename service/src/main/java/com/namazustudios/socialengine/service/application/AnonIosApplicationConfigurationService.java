package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.IosApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import com.namazustudios.socialengine.service.IosApplicationConfigurationService;

import javax.inject.Inject;

public class AnonIosApplicationConfigurationService implements IosApplicationConfigurationService {

    private IosApplicationConfigurationDao iosApplicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(final String applicationNameOrId,
                                               final String applicationProfileNameOrId) {
        throw new ForbiddenException();
    }

    @Override
    public IosApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                   final String applicationProfileNameOrId) {
        return getIosApplicationConfigurationDao().getIosApplicationConfiguration(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public IosApplicationConfiguration createApplicationConfiguration(final String applicationNameOrId,
                                                                      final IosApplicationConfiguration iosApplicationConfiguration) {
        throw new ForbiddenException();
    }

    @Override
    public IosApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                                      final String applicationConfigurationNameOrId,
                                                                      final IosApplicationConfiguration iosApplicationConfiguration) {
        throw new ForbiddenException();
    }

    public IosApplicationConfigurationDao getIosApplicationConfigurationDao() {
        return iosApplicationConfigurationDao;
    }

    @Inject
    public void setIosApplicationConfigurationDao(IosApplicationConfigurationDao iosApplicationConfigurationDao) {
        this.iosApplicationConfigurationDao = iosApplicationConfigurationDao;
    }

}
