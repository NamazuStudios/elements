package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.IosApplicationProfile;

/**
 * Created by patricktwohig on 5/25/17.
 */
public interface IosApplicationProfileDao {

    /**
     * Creates, or updates an inactive ApplicationProfile object.
     *
     * @param applicationNameOrId
     * @param iosApplicationProfile
     * @return
     */
    IosApplicationProfile createOrUpdateInactiveApplicationProfile(final String applicationNameOrId,
                                                                   final IosApplicationProfile iosApplicationProfile);

    /**
     * Gets an {@link IosApplicationProfile} with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationProfile} id
     */
    IosApplicationProfile getIosApplicationProfile(final String applicationNameOrId,
                                                   final String applicationProfileNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link IosApplicationProfile} id
     * @param iosApplicationProfile the {@link IosApplicationProfile} object to write
     *
     * @return the {@link IosApplicationProfile} object as it was persisted to the database.
     *
     */
    IosApplicationProfile updateApplicationProfile(final String applicationNameOrId,
                                                   final String applicationProfileNameOrId,
                                                   final IosApplicationProfile iosApplicationProfile);

    /**
     * Delets an {@link IosApplicationProfile} using the ID as reference.
     *
     *  @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link IosApplicationProfile} id
     *
     */
    void softDeleteApplicationProfile(final String applicationNameOrId, final String applicationProfileNameOrId);

}
