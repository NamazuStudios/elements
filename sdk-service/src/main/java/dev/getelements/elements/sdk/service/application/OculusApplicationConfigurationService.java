package dev.getelements.elements.sdk.service.application;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface OculusApplicationConfigurationService {

    /**
     * Deletes an {@link OculusApplicationConfiguration} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     *
     */
    void deleteApplicationConfiguration(final String applicationNameOrId, final String applicationConfigurationNameOrId);

    /**
     * Gets an application with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link OculusApplicationConfiguration} id
     *
     */
    OculusApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param oculusApplicationConfiguration the {@link OculusApplicationConfiguration} object to write
     * @return the {@link OculusApplicationConfiguration} object as it was persisted to the database.
     *
     */
    OculusApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final OculusApplicationConfiguration oculusApplicationConfiguration);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link OculusApplicationConfiguration} id
     * @param oculusApplicationConfiguration the {@link OculusApplicationConfiguration} object to write
     *
     * @return the {@link OculusApplicationConfiguration} object as it was persisted to the database.
     *
     */
    OculusApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final OculusApplicationConfiguration oculusApplicationConfiguration);

}
