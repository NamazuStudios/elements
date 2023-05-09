package dev.getelements.elements.service.application;

import dev.getelements.elements.dao.IosApplicationConfigurationDao;
import dev.getelements.elements.model.application.IosApplicationConfiguration;
import dev.getelements.elements.service.IosApplicationConfigurationService;

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
