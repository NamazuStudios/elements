package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.service.application.SteamApplicationConfigurationService;
import jakarta.inject.Inject;

public class AnonSteamApplicationConfigurationService implements SteamApplicationConfigurationService {

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(final String applicationNameOrId, final String applicationConfigurationNameOrId) {
        throw new ForbiddenException();
    }

    @Override
    public SteamApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var config = getApplicationConfigurationDao().getApplicationConfiguration(
                SteamApplicationConfiguration.class,
                applicationNameOrId,
                applicationConfigurationNameOrId);

        config.setPublisherKey(null);
        return config;
    }

    @Override
    public SteamApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final SteamApplicationConfiguration steamApplicationConfiguration) {
        throw new ForbiddenException();
    }

    @Override
    public SteamApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final SteamApplicationConfiguration steamApplicationConfiguration) {
        throw new ForbiddenException();
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

}
