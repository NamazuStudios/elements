package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;

/**
 * Created by patricktwohig on 6/14/17.
 */
public interface FacebookApplicationConfigurationDao {

    /**
     * Creates, or updates an inactive ApplicationConfiguration object.
     *
     * @param applicationNameOrId
     * @param iosApplicationProfile
     * @return
     */
    FacebookApplicationConfiguration createOrUpdateInactiveApplicationProfile(final String applicationNameOrId,
                                                                              final FacebookApplicationConfiguration iosApplicationProfile);

    /**
     * Gets an {@link FacebookApplicationConfiguration} with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationConfiguration} id
     */
    FacebookApplicationConfiguration getIosApplicationProfile(final String applicationNameOrId,
                                                         final String applicationProfileNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link FacebookApplicationConfiguration} id
     * @param iosApplicationProfile the {@link FacebookApplicationConfiguration} object to write
     *
     * @return the {@link FacebookApplicationConfiguration} object as it was persisted to the database.
     *
     */
    FacebookApplicationConfiguration updateApplicationProfile(final String applicationNameOrId,
                                                         final String applicationProfileNameOrId,
                                                         final FacebookApplicationConfiguration iosApplicationProfile);

    /**
     * Delets an {@link FacebookApplicationConfiguration} using the ID as reference.
     *
     *  @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link FacebookApplicationConfiguration} id
     *
     */
    void softDeleteApplicationProfile(final String applicationNameOrId,
                                      final String applicationProfileNameOrId);

}
