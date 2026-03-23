package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.SteamApplicationConfigurationService;
import jakarta.inject.Inject;

public class SuperUserSteamApplicationConfigurationService implements SteamApplicationConfigurationService {

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(final String applicationNameOrId, final String applicationConfigurationNameOrId) {
        getApplicationConfigurationDao().deleteApplicationConfiguration(
                SteamApplicationConfiguration.class,
                applicationNameOrId,
                applicationConfigurationNameOrId);
    }

    @Override
    public SteamApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        return getApplicationConfigurationDao().getApplicationConfiguration(
                SteamApplicationConfiguration.class,
                applicationNameOrId,
                applicationConfigurationNameOrId);
    }

    @Override
    public SteamApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final SteamApplicationConfiguration steamApplicationConfiguration) {
        return getApplicationConfigurationDao()
                .createApplicationConfiguration(applicationNameOrId, steamApplicationConfiguration);
    }

    @Override
    public SteamApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final SteamApplicationConfiguration steamApplicationConfiguration) {
        return getApplicationConfigurationDao()
                .updateApplicationConfiguration(applicationNameOrId, steamApplicationConfiguration);
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

}
