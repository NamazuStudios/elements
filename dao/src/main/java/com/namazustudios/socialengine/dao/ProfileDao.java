package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;

/**
 * Created by patricktwohig on 6/28/17.
 */
@Expose(module = {
    "namazu.elements.dao.profile",
    "namazu.socialengine.dao.profile",
})
public interface ProfileDao {

    /**
     * Gets actives profiles specifying the offset and the count.
     *
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link Profile} objects.
     */
    Pagination<Profile> getActiveProfiles(int offset, int count);

    /**
     * Gets actives profiles specifying the offset and the count, specifying a search filter.
     *
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link Profile} objects.
     */
    Pagination<Profile> getActiveProfiles(int offset, int count, String search);

    /**
     * Gets the specific active profile with the id, or throws a {@link NotFoundException} if the
     * profile can't be found.
     *
     * @return the {@link Profile} that was requested, never null
     */
    Profile getActiveProfile(String profileId);

    /**
     * Gets the application profile provided the combination of the user ID and the application
     * id.
     *
     * @param userId as returned by {@link User#getId()}
     * @param applicationId as returned by {@link Application#getId()}
     * @return the {@link Profile} associated with both, never null
     *
     * @throws {@link NotFoundException} if no such application and user combination matches
     */
    Profile getActiveProfile(String userId, String applicationId);

    /**
     * Updates the specific active profile with the id, or throws a {@link NotFoundException} if the
     * profile can't be found.  The {@link Profile#getId()} is used to key the profile being updated.
     *
     * @return the {@link Profile} as it was written into the database
     */
    Profile updateActiveProfile(Profile profile);

    /**
     * Creates or reactivates an inactive profile.  If the profile is active then this throws a
     * {@link DuplicateException}.  The newly created or reactivated profile will contain the
     * ID of the profile as requested.  The value of {@link Profile#getId()} will be ignored
     * and updates will be keyed using the {@link User} and {@link Application}.
     *
     * @return the {@link Profile} as it was written into the database
     */
    Profile createOrReactivateProfile(Profile profile);

    /**
     * Creates, reactivates, or refreshes a {@link Profile}.  This is similar to
     * {@link #createOrReactivateProfile(Profile)} except that it will upsert
     * the {@link Profile}.
     *
     * If the profile is already active, then this will perform minimal updates
     * of the profile as it is supplied.
     *
     * @param profile the user
     * @return the {@link Profile}, as written to the database
     */
    Profile createReactivateOrRefreshProfile(final Profile profile);

    /**
     * Deletes a profile by marking it as inactive.  Data is otherwise retained in the database.
     *
     * @param profileId the profile ID
     */
    void softDeleteProfile(String profileId);


}
