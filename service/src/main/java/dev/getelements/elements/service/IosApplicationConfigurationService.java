package dev.getelements.elements.service;

import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.application.ApplicationConfiguration;
import dev.getelements.elements.model.application.IosApplicationConfiguration;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Created by patricktwohig on 5/24/17.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.application.configuration.ios"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.unscoped.application.configuration.ios",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.application.configuration.ios",
                deprecated = @DeprecationDefinition("Use eci.elements.service.application.configuration.ios instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.application.configuration.ios",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.unscoped.application.configuration.ios instead.")
        )
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
