package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Map;
import java.util.Optional;

/**
 * Created by patricktwohig on 6/28/17.
 */
@ElementServiceExport
public interface ProfileDao {

    /**
     * Finds the profile with the supplied profile ID.  Returning null if no profile matches the requested profile ID.
     *
     * @param profileId the profile ID
     * @return the active profile with the supplied ID, or null if not found.
     */
    Optional<Profile> findActiveProfile(String profileId);

    /**
     * Finds the profile with the supplied profile ID.  Returning null if no profile matches the requested profile ID
     * and user ID.
     *
     * @param profileId the profileId
     * @param userId    the id of the profile
     * @return the active profile, or null if not found
     */
    Optional<Profile> findActiveProfileForUser(String profileId, String userId);

    /**
     * Gets actives profiles specifying the offset and the count.
     *
     * @param offset              the offset
     * @param count               the count
     * @param applicationNameOrId the application name or ID (may be null)
     * @param userId              the user ID (may be null)
     * @param lowerBoundTimestamp optional last login lower bound cutoff in ms (inclusive). If negative valued, defaults
     *                            to unix epoch.
     * @param upperBoundTimestamp optional last login upper bound cutoff in ms (inclusive). If negative valued, defaults
     *                            to current server time.
     * @return a {@link Pagination} of {@link Profile} objects.
     */
    Pagination<Profile> getActiveProfiles(int offset, int count,
                                          String applicationNameOrId, String userId,
                                          Long lowerBoundTimestamp, Long upperBoundTimestamp);

    /**
     * Gets actives profiles specifying the offset and the count, specifying a search filter.
     *
     * @param offset the offset
     * @param count  the count
     * @param search the search string
     * @return a {@link Pagination} of {@link Profile} objects.
     */
    Pagination<Profile> getActiveProfiles(
            int offset,
            int count,
            String search);

    /**
     * Gets the specific active profile with the id, or throws a {@link NotFoundException} if the
     * profile can't be found.
     *
     * @return the {@link Profile} that was requested, never null
     */
    Profile getActiveProfile(String profileId);

    /**
     * Updates the specific active profile with the id, or throws a {@link NotFoundException} if the
     * profile can't be found.  The {@link Profile#getId()} is used to key the profile being updated.
     *
     * @return the {@link Profile} as it was written into the database
     */
    default Profile updateActiveProfile(Profile profile) {
        return updateActiveProfile(profile, profile.getMetadata());
    }

    /**
     * Updates the specific active profile with the id, or throws a {@link NotFoundException} if the
     * profile can't be found.  The {@link Profile#getId()} is used to key the profile being updated.
     *
     * @param metadata the profile metadata
     * @return the {@link Profile} as it was written into the database
     */
    Profile updateActiveProfile(Profile profile, Map<String, Object> metadata);

    /**
     * Updates metadata for the specified {@link Profile}, ignoring changes to all other fields.
     *
     * @param profileId the profile
     * @param metadata  the metadata
     * @return the updated {@link Profile}
     */
    Profile updateMetadata(String profileId, Map<String, Object> metadata);

    /**
     * Updates metadata for the specified {@link Profile}, ignoring changes to all other fields.
     *
     * @param profile  the profile
     * @param metadata the metadata
     * @return the updated {@link Profile}
     */
    Profile updateMetadata(Profile profile, Map<String, Object> metadata);

    /**
     * Creates or reactivates an inactive profile.  If the profile is active then this throws a
     * {@link DuplicateException}.  The newly created or reactivated profile will contain the
     * ID of the profile as requested.  The value of {@link Profile#getId()} will be ignored
     * and updates will be keyed using the {@link User} and {@link Application}.
     *
     * @return the {@link Profile} as it was written into the database
     */
    default Profile createOrReactivateProfile(Profile profile) {
        return createOrReactivateProfile(profile, profile.getMetadata());
    }

    /**
     * Creates or reactivates an inactive profile.  If the profile is active then this throws a
     * {@link DuplicateException}.  The newly created or reactivated profile will contain the
     * ID of the profile as requested.  The value of {@link Profile#getId()} will be ignored
     * and updates will be keyed using the {@link User} and {@link Application}.
     *
     * @param metadata the profile metadata
     * @return the {@link Profile} as it was written into the database
     */
    Profile createOrReactivateProfile(Profile profile, Map<String, Object> metadata);

    /**
     * Creates, reactivates, or refreshes a {@link Profile}.  This is similar to
     * {@link #createOrReactivateProfile(Profile)} except that it will upsert the {@link Profile}.
     * <p>
     * If the profile is already active, then this will perform minimal updates of the profile as it is supplied.
     * Specifically, it will not update the display name, application, and user field. It will only update the image
     * url, if it was supplied.
     *
     * @param profile the user
     * @return the {@link Profile}, as written to the database
     */
    Profile createOrRefreshProfile(final Profile profile);

    /**
     * Deletes a profile by marking it as inactive.  Data is otherwise retained in the database.
     *
     * @param profileId the profile ID
     */
    void softDeleteProfile(String profileId);

}
