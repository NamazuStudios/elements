package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.follower.Follower;
import com.namazustudios.socialengine.model.friend.Friend;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;

/**
 * Allows access to instances of {@link Follower}.  This is responsible for determining the current {@link Profile} as well
 * and ensuring that the associated {@link Follower} instances are properly filtered and represented.
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
     * Gets a single instance of {@link Friend}.  Throws an exception if the supplied {@link Friend} is not found.
     *
     * @param follower the id, as obtained using {@link Friend#getId()}.
     *
     * @return the {@link Friend}, never null
     */
    void createFollower(Follower follower);

    /**
     * Deletes the supplied {@link Follower}, throwing an exception if the supplied id is not valid.
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
