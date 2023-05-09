package dev.getelements.elements.dao;

import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.follower.CreateFollowerRequest;
import dev.getelements.elements.model.profile.Profile;

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
     * Creates a single instance of {@link CreateFollowerRequest} for the supplied profile id and toFollow id.
     *
     * @param profileId
     * @param createFollowerRequest the object to insert into the db
     */
    void createFollowerForProfile(String profileId, CreateFollowerRequest createFollowerRequest);

    /**
     * Deletes the single instance of {@link CreateFollowerRequest} for the supplied profile id.  Throwing a
     * {@link NotFoundException} if the follower does not exist for the supplied profile id.
     *
     * @param profileId the id of the logged in profile
     * @param profileToUnfollowId the id of the profile to unfollow
     */
    void deleteFollowerForProfile(String profileId, String profileToUnfollowId);
}
