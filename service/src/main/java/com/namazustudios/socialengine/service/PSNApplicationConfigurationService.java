package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.PSNApplicationConfiguration;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Created by patricktwohig on 5/24/17.
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.application.configuration.psn")
})
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
