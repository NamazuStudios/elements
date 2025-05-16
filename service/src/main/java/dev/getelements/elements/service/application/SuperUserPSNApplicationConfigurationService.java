package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.PSNApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.PSNApplicationConfigurationService;
import jakarta.inject.Inject;

/**
 * Created by patricktwohig on 5/24/17.
 */
public class SuperUserPSNApplicationConfigurationService implements PSNApplicationConfigurationService {

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public PSNApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationProfileNameOrId) {
        return getApplicationConfigurationDao().getApplicationConfiguration(
                PSNApplicationConfiguration.class,
                applicationNameOrId,
                applicationProfileNameOrId
        );
    }

    @Override
    public PSNApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final PSNApplicationConfiguration psnApplicationProfile) {
        return getApplicationConfigurationDao().createApplicationConfiguration(
                applicationNameOrId,
                psnApplicationProfile
        );
    }

    @Override
    public PSNApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final PSNApplicationConfiguration psnApplicationProfile) {
        return getApplicationConfigurationDao().updateApplicationConfiguration(
                applicationNameOrId,
                psnApplicationProfile
        );
    }

    @Override
    public void deleteApplicationConfiguration(String applicationNameOrId, String applicationProfileNameOrId) {
        getApplicationConfigurationDao().deleteApplicationConfiguration(
                PSNApplicationConfiguration.class,
                applicationNameOrId,
                applicationProfileNameOrId);
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

}
