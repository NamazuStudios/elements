package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;

/**
 * Created by patricktwohig on 5/24/17.
 */
public interface GooglePlayApplicationConfigurationService {

    /**
     * Deletes an {@link GooglePlayApplicationConfiguration} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationConfiguration} id
     *
     */
    void deleteApplicationProfile(final String applicationNameOrId, final String applicationProfileNameOrId);

    /**
     * Gets an application with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationConfiguration} id
     *
     */
    GooglePlayApplicationConfiguration getApplicationProfile(final String applicationNameOrId,
                                                             final String applicationProfileNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param googlePlayApplicationProfile the {@link GooglePlayApplicationConfiguration} object to write
     * @return the {@link GooglePlayApplicationConfiguration} object as it was persisted to the database.
     *
     */
    GooglePlayApplicationConfiguration createApplicationProfile(final String applicationNameOrId,
                                                                final GooglePlayApplicationConfiguration googlePlayApplicationProfile);

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
    GooglePlayApplicationConfiguration updateApplicationProfile(final String applicationNameOrId,
                                                                final String applicationProfileNameOrId,
                                                                final GooglePlayApplicationConfiguration googlePlayApplicationProfile);

}
