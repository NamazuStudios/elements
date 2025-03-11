package dev.getelements.elements.sdk.service.application;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface MatchmakingApplicationConfigurationService {
    
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
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param matchmakingApplicationConfiguration the {@link MatchmakingApplicationConfiguration} object to write
     * @return the {@link MatchmakingApplicationConfiguration} object as it was persisted to the database.
     *
     */
    MatchmakingApplicationConfiguration createApplicationConfiguration(
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
