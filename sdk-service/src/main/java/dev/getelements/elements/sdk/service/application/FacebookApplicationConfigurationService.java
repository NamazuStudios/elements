package dev.getelements.elements.sdk.service.application;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Created by patricktwohig on 6/14/17.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface FacebookApplicationConfigurationService {

    /**
     * Deletes an {@link FacebookApplicationConfiguration} using the ID as reference.
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
     * @param applicationConfigurationNameOrId the {@link FacebookApplicationConfiguration} id
     *
     */
    FacebookApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param facebookApplicationConfiguration the {@link FacebookApplicationConfiguration} object to write
     * @return the {@link FacebookApplicationConfiguration} object as it was persisted to the database.
     *
     */
    FacebookApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final FacebookApplicationConfiguration facebookApplicationConfiguration);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link FacebookApplicationConfiguration} id
     * @param facebookApplicationConfiguration the {@link FacebookApplicationConfiguration} object to write
     *
     * @return the {@link FacebookApplicationConfiguration} object as it was persisted to the database.
     *
     */
    FacebookApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final FacebookApplicationConfiguration facebookApplicationConfiguration);

}
