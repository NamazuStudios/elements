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
     * @param friendId the id, as obtained using {@link Friend#getId()}.
     *
     * @return the {@link Friend}, never null
     */
    Friend getFriend(String friendId);

    /**
     * Deletes the supplied {@link Friend}, throwing an exception if the supplied id is not valid.
     *
     * @param friendId the id, as obtained using {@link Friend#getId()}.
     */
    void deleteFriend(String friendId);

}
