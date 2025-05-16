package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.GooglePlayApplicationConfigurationService;
import jakarta.inject.Inject;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class SuperUserGooglePlayApplicationConfigurationService implements GooglePlayApplicationConfigurationService {

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        getApplicationConfigurationDao().deleteApplicationConfiguration(
                GooglePlayApplicationConfiguration.class,
                applicationNameOrId,
                applicationConfigurationNameOrId);
    }

    @Override
    public GooglePlayApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        return getApplicationConfigurationDao().getApplicationConfiguration(
                GooglePlayApplicationConfiguration.class,
                applicationNameOrId,
                applicationConfigurationNameOrId
        );
    }

    @Override
    public GooglePlayApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {
        return getApplicationConfigurationDao().createApplicationConfiguration(
                applicationNameOrId,
                googlePlayApplicationConfiguration
        );
    }

    @Override
    public GooglePlayApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {
        return getApplicationConfigurationDao().updateApplicationConfiguration(
                applicationNameOrId,
                googlePlayApplicationConfiguration
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
