package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.IosApplicationProfile;

/**
 * Created by patricktwohig on 5/24/17.
 */
public interface IosApplicationProfileService {

    /**
     * Deletes an {@link IosApplicationProfile} using the ID as reference.
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
    IosApplicationProfile getApplicationProfile(final String applicationNameOrId,
                                                final String applicationProfileNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param iosApplicationProfile the {@link IosApplicationProfile} object to write
     * @return the {@link IosApplicationProfile} object as it was persisted to the database.
     *
     */
    IosApplicationProfile createApplicationProfile(final String applicationNameOrId,
                                                   final IosApplicationProfile iosApplicationProfile);

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

}
