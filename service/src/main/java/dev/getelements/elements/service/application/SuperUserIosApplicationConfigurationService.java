package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.IosApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.IosApplicationConfigurationService;
import jakarta.inject.Inject;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class SuperUserIosApplicationConfigurationService implements IosApplicationConfigurationService {

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(final String applicationNameOrId,
                                               final String applicationProfileNameOrId) {
        getApplicationConfigurationDao().deleteApplicationConfiguration(
                IosApplicationConfiguration.class,
                applicationNameOrId,
                applicationProfileNameOrId);
    }

    @Override
    public IosApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                   final String applicationProfileNameOrId) {
        return getApplicationConfigurationDao().getApplicationConfiguration(
                IosApplicationConfiguration.class,
                applicationNameOrId,
                applicationProfileNameOrId
        );
    }

    @Override
    public IosApplicationConfiguration createApplicationConfiguration(final String applicationNameOrId,
                                                                      final IosApplicationConfiguration iosApplicationConfiguration) {
        return getApplicationConfigurationDao().createApplicationConfiguration(
                applicationNameOrId,
                iosApplicationConfiguration
        );
    }

    @Override
    public IosApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                                      final String applicationConfigurationNameOrId,
                                                                      final IosApplicationConfiguration iosApplicationConfiguration) {
        return getApplicationConfigurationDao().updateApplicationConfiguration(
                applicationNameOrId,
                iosApplicationConfiguration
        );
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

}
