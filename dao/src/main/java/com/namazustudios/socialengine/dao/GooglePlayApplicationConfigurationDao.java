package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;

/**
 * Created by patricktwohig on 5/25/17.
 */
@Expose(module = "namazu.socialengine.dao.googleplay")
public interface GooglePlayApplicationConfigurationDao {

    /**
     * Creates, or updates an inactive ApplicationConfiguration object.
     *
     * @param applicationNameOrId
     * @param googlePlayApplicationConfiguration
     * @return
     */
    GooglePlayApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            String applicationNameOrId,
            GooglePlayApplicationConfiguration googlePlayApplicationConfiguration);

    /**
     * Gets an {@link GooglePlayApplicationConfiguration} with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     */
    GooglePlayApplicationConfiguration getApplicationConfiguration(
            String applicationNameOrId,
            String applicationConfigurationNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link GooglePlayApplicationConfiguration} id
     * @param googlePlayApplicationConfiguration the {@link GooglePlayApplicationConfiguration} object to write
     *
     * @return the {@link GooglePlayApplicationConfiguration} object as it was persisted to the database.
     *
     */
    GooglePlayApplicationConfiguration updateApplicationConfiguration(
            String applicationNameOrId,
            String applicationProfileNameOrId,
            GooglePlayApplicationConfiguration googlePlayApplicationConfiguration);

    /**
     * Deletes an {@link GooglePlayApplicationConfiguration} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link GooglePlayApplicationConfiguration} id
     *
     */
    void softDeleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

}
