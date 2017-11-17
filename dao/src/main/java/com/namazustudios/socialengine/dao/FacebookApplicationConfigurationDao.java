package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.annotation.Expose;
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
     * @param facebookApplicationConfiguration
     * @return
     */
    FacebookApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            String applicationNameOrId,
            FacebookApplicationConfiguration facebookApplicationConfiguration);

    /**
     * Gets an {@link FacebookApplicationConfiguration} with the specific name or identifier.  This
     * may accept either the facebook app ID, or the internal identifier for the configuration.
     *
     * @param applicationConfigurationNameOrId the {@link Application} name or id
     */
    FacebookApplicationConfiguration getApplicationConfiguration(String applicationConfigurationNameOrId);

    /**
     * Gets an {@link FacebookApplicationConfiguration} with the specific name or identifier
     * combined witht he supplied {@link Application} identifier
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     */
    FacebookApplicationConfiguration getApplicationConfiguration(
            String applicationNameOrId,
            String applicationConfigurationNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link FacebookApplicationConfiguration} id
     * @param facebookApplicationConfiguration the {@link FacebookApplicationConfiguration} object to write
     *
     * @return the {@link FacebookApplicationConfiguration} object as it was persisted to the database.
     *
     */
    FacebookApplicationConfiguration updateApplicationConfiguration(
            String applicationNameOrId,
            String applicationProfileNameOrId,
            FacebookApplicationConfiguration facebookApplicationConfiguration);

    /**
     * Delets an {@link FacebookApplicationConfiguration} using the ID as reference.
     *
     *  @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link FacebookApplicationConfiguration} id
     *
     */
    void softDeleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

}
