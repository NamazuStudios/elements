package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;

/**
 * A DAO to access and modify instances of {@Link MatchmakingApplicationConfiguration}.
 */
public interface MatchmakingApplicationConfigurationDao {

    /**
     * Deletes an {@link MatchmakingApplicationConfiguration} using the ID as reference.
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
    MatchmakingApplicationConfiguration getApplicationConfiguration(String applicationNameOrId,
                                                                    String applicationConfigurationNameOrId);

    /**
     * Creates an application configuration with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param matchmakingApplicationConfiguration the {@link MatchmakingApplicationConfiguration} object to write
     * @return the {@link MatchmakingApplicationConfiguration} object as it was persisted to the database.
     *
     */
    MatchmakingApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            String applicationNameOrId,
            MatchmakingApplicationConfiguration matchmakingApplicationConfiguration);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link MatchmakingApplicationConfiguration} id
     * @param matchmakingApplicationConfiguration the {@link MatchmakingApplicationConfiguration} object to write
     *
     * @return the {@link MatchmakingApplicationConfiguration} object as it was persisted to the database.
     *
     */
    MatchmakingApplicationConfiguration updateApplicationConfiguration(
            String applicationNameOrId,
            String applicationConfigurationNameOrId,
            MatchmakingApplicationConfiguration matchmakingApplicationConfiguration);

}
