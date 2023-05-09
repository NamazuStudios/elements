package dev.getelements.elements.service;

import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.application.ApplicationConfiguration;
import dev.getelements.elements.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Created by patricktwohig on 5/24/17.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.application.configuration.googleplay"),
    @ModuleDefinition(
        value = "namazu.elements.service.unscoped.application.configuration.googleplay",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
public interface GooglePlayApplicationConfigurationService {

    /**
     * Deletes an {@link GooglePlayApplicationConfiguration} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     *
     */
    void deleteApplicationConfiguration(final String applicationNameOrId, final String applicationConfigurationNameOrId);

    /**
     * Gets an application with the specific name or identifier.
     *  @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     *
     */
    GooglePlayApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                   final String applicationConfigurationNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param googlePlayApplicationConfiguration the {@link GooglePlayApplicationConfiguration} object to write
     * @return the {@link GooglePlayApplicationConfiguration} object as it was persisted to the database.
     *
     */
    GooglePlayApplicationConfiguration createApplicationConfiguration(final String applicationNameOrId,
                                                                      final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link GooglePlayApplicationConfiguration} id
     * @param googlePlayApplicationConfiguration the {@link GooglePlayApplicationConfiguration} object to write
     *
     * @return the {@link GooglePlayApplicationConfiguration} object as it was persisted to the database.
     *
     */
    GooglePlayApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                                      final String applicationConfigurationNameOrId,
                                                                      final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration);

}
