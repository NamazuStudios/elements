package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;

/**
 * Created by patricktwohig on 5/25/17.
 */
public interface GooglePlayApplicationConfigurationDao {

    /**
     * Creates, or updates an inactive ApplicationConfiguration object.
     *
     * @param applicationNameOrId
     * @param googlePlayApplicationProfile
     * @return
     */
    GooglePlayApplicationConfiguration createOrUpdateInactiveApplicationProfile(
            final String applicationNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationProfile);

    /**
     * Gets an {@link GooglePlayApplicationConfiguration} with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationConfiguration} id
     */
    GooglePlayApplicationConfiguration getGooglePlayApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link GooglePlayApplicationConfiguration} id
     * @param googlePlayApplicationProfile the {@link GooglePlayApplicationConfiguration} object to write
     *
     * @return the {@link GooglePlayApplicationConfiguration} object as it was persisted to the database.
     *
     */
    GooglePlayApplicationConfiguration updateApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationProfile);

    /**
     * Delets an {@link GooglePlayApplicationConfiguration} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link GooglePlayApplicationConfiguration} id
     *
     */
    void softDeleteApplicationProfile(final String applicationNameOrId, final String applicationProfileNameOrId);

}
