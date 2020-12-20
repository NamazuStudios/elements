package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Created by patricktwohig on 5/24/17.
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.scoped.application.configuration.ios"),
    @ExposedModuleDefinition(value = "namazu.elements.service.unscoped.application.configuration.ios", annotation = Unscoped.class)
})
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
