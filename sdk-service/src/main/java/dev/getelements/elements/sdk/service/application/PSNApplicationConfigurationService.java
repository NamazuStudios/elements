package dev.getelements.elements.sdk.service.application;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.PSNApplicationConfiguration;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Created by patricktwohig on 5/24/17.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface PSNApplicationConfigurationService {

    /**
     * Deletes a {@link PSNApplicationConfiguration} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link PSNApplicationConfiguration} id
     *
     */
    void deleteApplicationConfiguration(final String applicationNameOrId, final String applicationProfileNameOrId);

    /**
     * Gets an application with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link PSNApplicationConfiguration} id
     *
     */
    PSNApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                            final String applicationProfileNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param psnApplicationProfile the {@link PSNApplicationConfiguration} object to write
     * @return the {@link PSNApplicationConfiguration} object as it was persisted to the database.
     *
     */
    PSNApplicationConfiguration createApplicationConfiguration(final String applicationNameOrId,
                                                               final PSNApplicationConfiguration psnApplicationProfile);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link PSNApplicationConfiguration} id
     * @param psnApplicationProfile the {@link PSNApplicationConfiguration} object to write
     *
     * @return the {@link PSNApplicationConfiguration} object as it was persisted to the database.
     *
     */
    PSNApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                               final String applicationProfileNameOrId,
                                                               final PSNApplicationConfiguration psnApplicationProfile);

}
