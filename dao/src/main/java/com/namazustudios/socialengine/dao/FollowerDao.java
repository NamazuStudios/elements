package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.follower.Follower;
import com.namazustudios.socialengine.model.friend.Friend;
import com.namazustudios.socialengine.model.profile.Profile;

public interface FollowerDao {

    /**
     * Fetches all {@link Profile} instances for the supplied profile id.
     *
     * @param profileId the id of the logged in profile
     * @param offset the offset
     * @param count the number of results to return
     * @return a {@link Pagination <Follower>}
     */
    Pagination<Profile> getFollowersForProfile(String profileId, int offset, int count);

    /**
     * Fetches {@link Profile} instance for the supplied profile id and followed id. Throwing a {@link NotFoundException}
     * if the follower does not exist for the supplied profile id.
     *
     * @param profileId the id of the logged in profile
     * @param followedId the id of the followed profile
     * @return a {@link Profile}
     */
    Profile getFollowerForProfile(String profileId, String followedId);

    /**
     * Creates a single instance of {@link Follower} for the supplied profile id and toFollow id.
     *
     * @param follower the object to insert into the db
     */
    void createFollowerForProfile(Follower follower);

    /**
     * Deletes the single instance of {@link Follower} for the supplied profile id.  Throwing a
     * {@link NotFoundException} if the follower does not exist for the supplied profile id.
     *
     * @param profileId the id of the logged in profile
     * @param profileToUnfollowId the id of the profile to unfollow
     */
    void deleteFollowerForProfile(String profileId, String profileToUnfollowId);
}
