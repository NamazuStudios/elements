package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;

/**
 * A DAO to access and modify instances of {@Link MatchmakingApplicationConfiguration}.
 */
public interface MatchmakingApplicationConfigurationDao {

    /**
     * Creates, or updates an inactive ApplicationConfiguration object.
     *
     * @param applicationNameOrId
     * @param matchmakingApplicationConfiguration
     * @return
     */
    MatchmakingApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            String applicationNameOrId,
            MatchmakingApplicationConfiguration matchmakingApplicationConfiguration);

    /**
     * Gets an {@link MatchmakingApplicationConfiguration} with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     */
    MatchmakingApplicationConfiguration getApplicationConfiguration(String applicationNameOrId,
                                                                    String applicationConfigurationNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link MatchmakingApplicationConfiguration} id
     * @param matchmakingApplicationConfiguration the {@link MatchmakingApplicationConfiguration} object to write
     *
     * @return the {@link MatchmakingApplicationConfiguration} object as it was persisted to the database.
     *
     */
    MatchmakingApplicationConfiguration updateApplicationConfiguration(
            String applicationNameOrId,
            String applicationProfileNameOrId,
            MatchmakingApplicationConfiguration matchmakingApplicationConfiguration);

    /**
     * Deletes a {@link MatchmakingApplicationConfiguration} using the ID as reference.
     *
     *  @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link MatchmakingApplicationConfiguration} id
     *
     */
    void softDeleteApplicationConfiguration(String applicationNameOrId,
                                            String applicationConfigurationNameOrId);

}
