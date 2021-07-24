package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;

/**
 * Manages instances of {@link GameOnApplicationConfiguration}.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.application.configuration.gameon"),
    @ModuleDefinition(
        value = "namazu.elements.service.unscoped.application.configuration.gameon",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
public interface GameOnApplicationConfigurationService {

    /**
     * Gets an instance of {@link GameOnApplicationConfiguration} for the supplied application and configuration name.
     * @param applicationNameOrId the name or ID of the {@link Application}
     * @param applicationConfigurationNameOrId the name or ID of the {@link GameOnApplicationConfiguration}
     * @return the {@link GameOnApplicationConfiguration}, never null
     * @throws {@link ResourceNotFoundException} if the configuration does not exist.
     */
    GameOnApplicationConfiguration getApplicationConfiguration(
        String applicationNameOrId,
        String applicationConfigurationNameOrId);

    /**
     * Creates an instance of {@link GameOnApplicationConfiguration} for the supplied application and configuration name.
     *
     * @param applicationNameOrId the name or ID of the {@link Application}
     * @param gameOnApplicationConfiguration the {@link GameOnApplicationConfiguration} to create
     * @return the {@link GameOnApplicationConfiguration}, never null, as it was written to the database
     */
    GameOnApplicationConfiguration createApplicationConfiguration(
        String applicationNameOrId,
        GameOnApplicationConfiguration gameOnApplicationConfiguration);

    /**
     * Updates an instance of {@link GameOnApplicationConfiguration} for the supplied application and configuration name.
     *
     * @param applicationNameOrId the name or ID of the {@link Application}
     * @param gameOnApplicationConfiguration the {@link GameOnApplicationConfiguration} to create
     * @return the {@link GameOnApplicationConfiguration}, never null, as it was written to the database
     */
    GameOnApplicationConfiguration updateApplicationConfiguration(
        String applicationNameOrId,
        String applicationConfigurationNameOrId,
        GameOnApplicationConfiguration gameOnApplicationConfiguration);

    /**
     * Deletes an instance of {@link GameOnApplicationConfiguration} with the supplied application name/id and
     * configuration name/id.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link GameOnApplicationConfiguration} name or id.
     */
    void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

}
