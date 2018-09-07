package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.exception.gameon.GameOnApplicationConfigurationNotFoundException;
import com.namazustudios.socialengine.exception.gameon.GameOnConfigurationException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;

import java.util.List;

/**
 * Accesss and manages instances of {@link GameOnApplicationConfiguration} in the database.
 */
@Expose(module = {
    "namazu.elements.dao.application.configuration.gameon",
    "namazu.socialengine.dao.application.configuration.gameon"
})
public interface GameOnApplicationConfigurationDao {

    /**
     * Gets an instance of {@link GameOnApplicationConfiguration} from the database.
     *
     * @param applicationNameOrId the name or id of the application ({@link Application#getId()} or {@link Application#getName()})
     * @param applicationConfigurationNameOrId the name of the ID of the configuration ({@link ApplicationConfiguration#getId()} {@link ApplicationConfiguration#getUniqueIdentifier()})
     * @return the {@link GameOnApplicationConfiguration} instance
     */
    GameOnApplicationConfiguration getApplicationConfiguration(
        String applicationNameOrId,
        String applicationConfigurationNameOrId);

    /**
     * Gets all instances of {@link GameOnApplicationConfiguration} from the database for the supplied
     * {@link Application} name or .d
     *
     * @param applicationNameOrId the name or id of the application ({@link Application#getId()} or {@link Application#getName()})
     * @return the {@link GameOnApplicationConfiguration} instance
     */
    List<GameOnApplicationConfiguration> getConfigurationsForApplication(String applicationNameOrId);

    /**
     * Gets the default {@link GameOnApplicationConfiguration} for the supplied application name or id.  The default
     * implementation just selects the first one it finds using a call to {@link #getConfigurationsForApplication(String)}
     * throwing an exception if too many or too few are configured.  Subclasses may refine this behavior to allow for
     * more fine-grained control or efficient database lookups.
     *
     * @param applicationNameOrId the name or id of the application ({@link Application#getId()} or {@link Application#getName()})
     * @return the first {@link GameOnApplicationConfiguration}
     *
     * @throws {@link GameOnApplicationConfigurationNotFoundException} if the {@link GameOnApplicationConfiguration} is not found
     * @throws {@link GameOnConfigurationException} if multiple {@link GameOnApplicationConfiguration} are configured for the application
     */
    default GameOnApplicationConfiguration getDefaultConfigurationForApplication(final String applicationNameOrId) {

        final List<GameOnApplicationConfiguration> gameOnApplicationConfigurationList;
        gameOnApplicationConfigurationList = getConfigurationsForApplication(applicationNameOrId);

        if (gameOnApplicationConfigurationList.isEmpty()) {
            throw new GameOnApplicationConfigurationNotFoundException("No GameOn configuration for " + applicationNameOrId);
        } else if (gameOnApplicationConfigurationList.size() > 1) {
            throw new GameOnConfigurationException(gameOnApplicationConfigurationList.size() + " GameOn configurations for " + applicationNameOrId);
        } else {
            return gameOnApplicationConfigurationList.get(0);
        }

    }

    /**
     * Creates or update an inactive application configuration for GameOn.
     *
     * @param applicationNameOrId the name or id of the application ({@link Application#getId()} or {@link Application#getName()})
     * @return the {@link GameOnApplicationConfiguration} instance as it was written
     */
    GameOnApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
        String applicationNameOrId,
        GameOnApplicationConfiguration gameOnApplicationConfiguration);

    /**
     * Updates an active application configuration for GameOn.
     *
     * @param applicationNameOrId the name or id of the application ({@link Application#getId()} or {@link Application#getName()})
     * @param applicationConfigurationNameOrId the name of the ID of the configuration ({@link ApplicationConfiguration#getId()} {@link ApplicationConfiguration#getUniqueIdentifier()})
     * @return the {@link GameOnApplicationConfiguration} instance as it was written
     */
    GameOnApplicationConfiguration updateApplicationConfiguration(
        String applicationNameOrId,
        String applicationConfigurationNameOrId,
        GameOnApplicationConfiguration gameOnApplicationConfiguration);

    /**
     * Soft deletes an instance of {@link GameOnApplicationConfiguration}, by marking it inactive in the database.
     *
     * @param applicationNameOrId the name or id of the application ({@link Application#getId()} or {@link Application#getName()})
     * @param applicationConfigurationNameOrId the name of the ID of the configuration ({@link ApplicationConfiguration#getId()} {@link ApplicationConfiguration#getUniqueIdentifier()})
     */
    void softDeleteApplicationConfiguration(
        String applicationNameOrId,
        String applicationConfigurationNameOrId);

}
