package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Created by patricktwohig on 5/25/17.
 */
@Expose({
    @ExposedModuleDefinition("namazu.elements.dao.ios"),
    @ExposedModuleDefinition(
        value = "namazu.socialengine.dao.ios",
        deprecated = @DeprecationDefinition("Use namazu.elements.dao.ios instead"))
})
public interface IosApplicationConfigurationDao {

    /**
     * Creates, or updates an inactive ApplicationConfiguration object.
     *
     * @param applicationNameOrId
     * @param iosApplicationConfiguration
     * @return
     */
    IosApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final IosApplicationConfiguration iosApplicationConfiguration);

    /**
     * Gets an {@link IosApplicationConfiguration} with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     */
    IosApplicationConfiguration getIosApplicationConfiguration(final String applicationNameOrId,
                                                               final String applicationConfigurationNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link IosApplicationConfiguration} id
     * @param iosApplicationConfiguration the {@link IosApplicationConfiguration} object to write
     *
     * @return the {@link IosApplicationConfiguration} object as it was persisted to the database.
     *
     */
    IosApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                               final String applicationProfileNameOrId,
                                                               final IosApplicationConfiguration iosApplicationConfiguration);

    /**
     * Delets an {@link IosApplicationConfiguration} using the ID as reference.
     *
     *  @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link IosApplicationConfiguration} id
     *
     */
    void softDeleteApplicationConfiguration(final String applicationNameOrId,
                                            final String applicationConfigurationNameOrId);

}
