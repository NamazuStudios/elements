package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;

/**
 * Created by patricktwohig on 7/13/15.
 */
public interface ApplicationProfileDao {

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
     * Gets the active applications registered in the databse given the offset and count.
     *
     * @param offset the offset
     * @param count the count
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationProfile> getActiveApplicationProfiles(final String applicationNameOrId,
                                                                final int offset, final int count);

    /**
     * Gets the active applications registered in the databse given the offset and count.
     *
     * @param offset the offset
     * @param count the count
     * @param search a query to filter the results
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationProfile> getActiveApplicationProfiles(final String applicationNameOrId,
                                                                final int offset, final int count, final String search);

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
     * Delets an {@link ApplicationProfile} using the ID as reference.
     *  @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationProfile} id
     *
     */
    void softDeleteApplicationProfile(final String applicationNameOrId, final String applicationProfileNameOrId);

}
