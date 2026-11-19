package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.service.application.SteamApplicationConfigurationService;
import jakarta.inject.Inject;

import java.util.List;

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

        // One Steam configuration per application: the verification flow resolves the configuration for
        // the application and throws when more than one is active, so a second config would fail every
        // Steam receipt upload with a confusing error instead of a clear one.
        final var existing = getApplicationConfigurationDao()
                .getAllActiveApplicationConfigurations(applicationNameOrId, SteamApplicationConfiguration.class);

        if (!existing.isEmpty()) {
            throw new InvalidDataException("The application already has a Steam application configuration. "
                    + "An application may have only one Steam configuration; update the existing one instead"
                    + (existing.getFirst().getName() == null
                        ? "."
                        : " ('" + existing.getFirst().getName() + "')."));
        }

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
