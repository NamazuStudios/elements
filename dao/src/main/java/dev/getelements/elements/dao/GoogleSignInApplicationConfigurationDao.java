package dev.getelements.elements.dao;

import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.application.ApplicationConfiguration;
import dev.getelements.elements.model.application.GoogleSignInApplicationConfiguration;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Created by patricktwohig on 5/25/17.
 */
@Expose({
        @ModuleDefinition("eci.elements.dao.googleplay"),
        @ModuleDefinition(
                value = "namazu.elements.dao.googleplay",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.googleplay instead")
        )
})
public interface GoogleSignInApplicationConfigurationDao {

    /**
     * Creates, or updates an inactive ApplicationConfiguration object.
     *
     * @param applicationNameOrId
     * @param googleSignInApplicationConfiguration
     * @return
     */
    GoogleSignInApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            String applicationNameOrId,
            GoogleSignInApplicationConfiguration googleSignInApplicationConfiguration);

    /**
     * Gets an {@link GoogleSignInApplicationConfiguration} with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     */
    GoogleSignInApplicationConfiguration getApplicationConfiguration(
            String applicationNameOrId,
            String applicationConfigurationNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link GoogleSignInApplicationConfiguration} id
     * @param googleSignInApplicationConfiguration the {@link GoogleSignInApplicationConfiguration} object to write
     *
     * @return the {@link GoogleSignInApplicationConfiguration} object as it was persisted to the database.
     *
     */
    GoogleSignInApplicationConfiguration updateApplicationConfiguration(
            String applicationNameOrId,
            String applicationProfileNameOrId,
            GoogleSignInApplicationConfiguration googleSignInApplicationConfiguration);

    /**
     * Deletes an {@link GoogleSignInApplicationConfiguration} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link GoogleSignInApplicationConfiguration} id
     *
     */
    void softDeleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

}
