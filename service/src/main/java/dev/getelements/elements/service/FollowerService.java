package dev.getelements.elements.service;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.follower.CreateFollowerRequest;
import dev.getelements.elements.model.friend.Friend;
import dev.getelements.elements.model.profile.Profile;

/**
 * Allows access to instances of {@link CreateFollowerRequest}.  This is responsible for determining the current {@link Profile} as well
 * and ensuring that the associated {@link CreateFollowerRequest} instances are properly filtered and represented.
 */
public interface FollowerService {

    /**
     * Gets the listing of {@link Profile} with the supplied offset and count.
     *
     * @param profileId the profile id to fetch followers for
     * @param offset the offset
     * @param count the count
     * @return the list of {@link Profile} instances
     */
    Pagination<Profile> getFollowers(String profileId, int offset, int count);

    /**
     * Gets the listing of {@link Profile} with the supplied offset, count, and search query.
     *
     * @param profileId the profile id to fetch the follower for
     * @param followedId the specific follower id to get the profile for
     * @return {@link Profile} instance
     */
    Profile getFollower(String profileId, String followedId);

    /**
     * Creates a follower link between two profiles
     *
     * @param profileId the id of the user profile
     * @param createFollowerRequest the request body containing the the id of the profile to follow.
     */
    void createFollower(String profileId, CreateFollowerRequest createFollowerRequest);

    /**
     * Deletes the link between the supplied profile id's, throwing an exception if the supplied id is not valid.
     *
     * @param profileId the id of the user profile.
     * @param profileToUnfollowId the id of the profile to unfollow
     */
    void deleteFollower(String profileId, String profileToUnfollowId);

    /**
     * Redacts any private information from the {@link Profile} and returns either a new instance or the current
     * instance modified.
     *
     * The default implementation simply sets the user to null and returns the supplied instance.  Other implementations
     * such as those used for superuser access may redact information differently.
     *
     * @param profile the {@link Profile} from which to redact private information
     * @return a {@link Profile} with the information redacted (may be the same instance provided
     */
    default Profile redactPrivateInformation(final Profile profile) {
        profile.setUser(null);
        return profile;
    }
}
