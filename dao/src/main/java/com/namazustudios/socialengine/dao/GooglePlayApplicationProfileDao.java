package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationProfile;

/**
 * Created by patricktwohig on 5/25/17.
 */
public interface GooglePlayApplicationProfileDao {

    /**
     * Creates, or updates an inactive ApplicationProfile object.
     *
     * @param applicationNameOrId
     * @param googlePlayApplicationProfile
     * @return
     */
    GooglePlayApplicationProfile createOrUpdateInactiveApplicationProfile(
            final String applicationNameOrId,
            final GooglePlayApplicationProfile googlePlayApplicationProfile);

    /**
     * Gets an {@link GooglePlayApplicationProfile} with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationProfile} id
     */
    GooglePlayApplicationProfile getGooglePlayApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link GooglePlayApplicationProfile} id
     * @param googlePlayApplicationProfile the {@link GooglePlayApplicationProfile} object to write
     *
     * @return the {@link GooglePlayApplicationProfile} object as it was persisted to the database.
     *
     */
    GooglePlayApplicationProfile updateApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final GooglePlayApplicationProfile googlePlayApplicationProfile);

    /**
     * Delets an {@link GooglePlayApplicationProfile} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link GooglePlayApplicationProfile} id
     *
     */
    void softDeleteApplicationProfile(final String applicationNameOrId, final String applicationProfileNameOrId);

}
