package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;

/**
 * Manages the lifecycle of the various {@link ApplicationProfile} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
public interface ApplicationProfileService {

    /**
     * Gets the applications registered in the databse given the offset and count.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param offset the offset
     * @param count the count
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationProfile> getApplicationProfiles(final String applicationNameOrId,
                                                          final int offset, final int count);

    /**
     * Gets the applications registered in the databse given the offset and count.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param offset the offset
     * @param count the count
     * @param search a query to filter the results
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationProfile> getApplicationProfiles(final String applicationNameOrId,
                                                          final int offset, final int count, final String search);

    /**
     * Gets an application with the specific name or identifier.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationProfile} id
     *
     */
    ApplicationProfile getApplicationProfile(final String applicationNameOrId, final String applicationProfileNameOrId);

    /**
     * Gets an application with the specific name or identifier.  Additionally,
     * this checks that the type of {@link ApplicationProfile} is of the given type or subtype.  IF
     * the type does not match an instance of {@link NotFoundException} is thrown.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationProfile} id
     * @param type the type of profile to find
     */
    <T extends ApplicationProfile> T getApplicationProfile(final String applicationNameOrId,
                                                           final String applicationProfileNameOrId,
                                                           final Class<T> type);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param psnApplicationProfile the {@link PSNApplicationProfile} object to write
     * @return the {@link PSNApplicationProfile} object as it was persisted to the database.
     *
     */
    PSNApplicationProfile createApplicationProfile(final String applicationNameOrId,
                                                   final PSNApplicationProfile psnApplicationProfile);

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
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link ApplicationProfile} id
     *
     */
    void deleteApplicationProfile(final String applicationNameOrId, final String applicationProfileNameOrId,
                                  final Class<? extends ApplicationProfile> type);

}
