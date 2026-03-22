package dev.getelements.elements.sdk.service.application;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface SteamApplicationConfigurationService {

    /**
     * Deletes a {@link SteamApplicationConfiguration} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     */
    void deleteApplicationConfiguration(final String applicationNameOrId, final String applicationConfigurationNameOrId);

    /**
     * Gets a Steam application configuration with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link SteamApplicationConfiguration} id
     */
    SteamApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId);

    /**
     * Creates a new Steam application configuration.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param steamApplicationConfiguration the {@link SteamApplicationConfiguration} object to write
     * @return the {@link SteamApplicationConfiguration} object as it was persisted to the database
     */
    SteamApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final SteamApplicationConfiguration steamApplicationConfiguration);

    /**
     * Updates an existing Steam application configuration.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link SteamApplicationConfiguration} id
     * @param steamApplicationConfiguration the {@link SteamApplicationConfiguration} object to write
     * @return the {@link SteamApplicationConfiguration} object as it was persisted to the database
     */
    SteamApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final SteamApplicationConfiguration steamApplicationConfiguration);

}
