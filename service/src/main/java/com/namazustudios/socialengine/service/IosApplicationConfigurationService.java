package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;

/**
 * Created by patricktwohig on 5/24/17.
 */
public interface IosApplicationConfigurationService {

    /**
     * Deletes an {@link IosApplicationConfiguration} using the ID as reference.
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
    IosApplicationConfiguration getApplicationProfile(final String applicationNameOrId,
                                                      final String applicationProfileNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param iosApplicationProfile the {@link IosApplicationConfiguration} object to write
     * @return the {@link IosApplicationConfiguration} object as it was persisted to the database.
     *
     */
    IosApplicationConfiguration createApplicationProfile(final String applicationNameOrId,
                                                         final IosApplicationConfiguration iosApplicationProfile);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link IosApplicationConfiguration} id
     * @param iosApplicationProfile the {@link IosApplicationConfiguration} object to write
     *
     * @return the {@link IosApplicationConfiguration} object as it was persisted to the database.
     *
     */
    IosApplicationConfiguration updateApplicationProfile(final String applicationNameOrId,
                                                         final String applicationProfileNameOrId,
                                                         final IosApplicationConfiguration iosApplicationProfile);

}
