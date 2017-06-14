package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.PSNApplicationConfiguration;

/**
 * Created by patricktwohig on 5/25/17.
 */
public interface PSNApplicationConfigurationDao {

    /**
     * Creates, or updates an inactive ApplicationConfiguration object.
     *
     * @param applicationNameOrId
     * @param psnApplicationProfile
     * @return
     */
    PSNApplicationConfiguration createOrUpdateInactiveApplicationProfile(final String applicationNameOrId,
                                                                         final PSNApplicationConfiguration psnApplicationProfile);

    /**
     * Gets an {@link PSNApplicationConfiguration} with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationConfiguration} id
     */
    PSNApplicationConfiguration getPSNApplicationProfile(final String applicationNameOrId,
                                                         final String applicationProfileNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link PSNApplicationConfiguration} id
     * @param psnApplicationProfile the {@link PSNApplicationConfiguration} object to write
     *
     * @return the {@link PSNApplicationConfiguration} object as it was persisted to the database.
     *
     */
    PSNApplicationConfiguration updateApplicationProfile(final String applicationNameOrId,
                                                         final String applicationProfileNameOrId,
                                                         final PSNApplicationConfiguration psnApplicationProfile);

    /**
     * Delets an {@link PSNApplicationConfiguration} using the ID as reference.
     *
     *  @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link PSNApplicationConfiguration} id
     *
     */
    void softDeleteApplicationProfile(final String applicationNameOrId, final String applicationProfileNameOrId);

}
