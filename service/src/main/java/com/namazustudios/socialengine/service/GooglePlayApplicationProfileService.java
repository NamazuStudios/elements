package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationProfile;

/**
 * Created by patricktwohig on 5/24/17.
 */
public interface GooglePlayApplicationProfileService {

    /**
     * Deletes an {@link GooglePlayApplicationProfile} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationProfile} id
     *
     */
    void deleteApplicationProfile(final String applicationNameOrId, final String applicationProfileNameOrId);

    /**
     * Gets an application with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationProfile} id
     *
     */
    GooglePlayApplicationProfile getApplicationProfile(final String applicationNameOrId,
                                                       final String applicationProfileNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param googlePlayApplicationProfile the {@link GooglePlayApplicationProfile} object to write
     * @return the {@link GooglePlayApplicationProfile} object as it was persisted to the database.
     *
     */
    GooglePlayApplicationProfile createApplicationProfile(final String applicationNameOrId,
                                                          final GooglePlayApplicationProfile googlePlayApplicationProfile);

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
    GooglePlayApplicationProfile updateApplicationProfile(final String applicationNameOrId,
                                                          final String applicationProfileNameOrId,
                                                          final GooglePlayApplicationProfile googlePlayApplicationProfile);

}
