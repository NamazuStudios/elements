package dev.getelements.elements.service;

import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.application.ApplicationConfiguration;
import dev.getelements.elements.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Created by patricktwohig on 6/14/17.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.application.configuration.facebook"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.unscoped.application.configuration.facebook",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.application.configuration.facebook",
                deprecated = @DeprecationDefinition("Use eci.elements.service.application.configuration.facebook instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.application.configuration.facebook",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.unscoped.application.configuration.facebook instead.")
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
