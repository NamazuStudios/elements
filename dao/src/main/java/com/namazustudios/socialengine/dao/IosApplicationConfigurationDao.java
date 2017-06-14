package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;

/**
 * Created by patricktwohig on 5/25/17.
 */
public interface IosApplicationConfigurationDao {

    /**
     * Creates, or updates an inactive ApplicationConfiguration object.
     *
     * @param applicationNameOrId
     * @param iosApplicationProfile
     * @return
     */
    IosApplicationConfiguration createOrUpdateInactiveApplicationProfile(final String applicationNameOrId,
                                                                         final IosApplicationConfiguration iosApplicationProfile);

    /**
     * Gets an {@link IosApplicationConfiguration} with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationConfiguration} id
     */
    IosApplicationConfiguration getIosApplicationProfile(final String applicationNameOrId,
                                                         final String applicationProfileNameOrId);

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

    /**
     * Delets an {@link IosApplicationConfiguration} using the ID as reference.
     *
     *  @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link IosApplicationConfiguration} id
     *
     */
    void softDeleteApplicationProfile(final String applicationNameOrId,
                                      final String applicationProfileNameOrId);

}
