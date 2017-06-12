package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;

/**
 * Created by patricktwohig on 5/25/17.
 */
public interface PSNApplicationProfileDao {

    /**
     * Creates, or updates an inactive ApplicationProfile object.
     *
     * @param applicationNameOrId
     * @param psnApplicationProfile
     * @return
     */
    PSNApplicationProfile createOrUpdateInactiveApplicationProfile(final String applicationNameOrId,
                                                                   final PSNApplicationProfile psnApplicationProfile);

    /**
     * Gets an {@link PSNApplicationProfile} with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationProfile} id
     */
    PSNApplicationProfile getPSNApplicationProfile(final String applicationNameOrId,
                                                   final String applicationProfileNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link PSNApplicationProfile} id
     * @param psnApplicationProfile the {@link PSNApplicationProfile} object to write
     *
     * @return the {@link PSNApplicationProfile} object as it was persisted to the database.
     *
     */
    PSNApplicationProfile updateApplicationProfile(final String applicationNameOrId,
                                                   final String applicationProfileNameOrId,
                                                   final PSNApplicationProfile psnApplicationProfile);

    /**
     * Delets an {@link PSNApplicationProfile} using the ID as reference.
     *
     *  @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link PSNApplicationProfile} id
     *
     */
    void softDeleteApplicationProfile(final String applicationNameOrId, final String applicationProfileNameOrId);

}
