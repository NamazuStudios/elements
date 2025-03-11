package dev.getelements.elements.sdk.service.application;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.IosApplicationConfiguration;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Created by patricktwohig on 5/24/17.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface IosApplicationConfigurationService {

    /**
     * Deletes an {@link IosApplicationConfiguration} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     *
     */
    void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

    /**
     * Gets an application with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     *
     */
    IosApplicationConfiguration getApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param iosApplicationConfiguration the {@link IosApplicationConfiguration} object to write
     * @return the {@link IosApplicationConfiguration} object as it was persisted to the database.
     *
     */
    IosApplicationConfiguration createApplicationConfiguration(final String applicationNameOrId,
                                                               final IosApplicationConfiguration iosApplicationConfiguration);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link IosApplicationConfiguration} id
     * @param iosApplicationConfiguration the {@link IosApplicationConfiguration} object to write
     *
     * @return the {@link IosApplicationConfiguration} object as it was persisted to the database.
     *
     */
    IosApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                               final String applicationConfigurationNameOrId,
                                                               final IosApplicationConfiguration iosApplicationConfiguration);

}
