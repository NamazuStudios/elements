package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

/**
 * Created by patricktwohig on 6/14/17.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.application.configuration.facebook"),
    @ModuleDefinition(
        value = "namazu.elements.service.unscoped.application.configuration.facebook",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
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
