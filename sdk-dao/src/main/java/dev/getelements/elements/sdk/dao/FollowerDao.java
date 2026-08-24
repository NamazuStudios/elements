package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.follower.CreateFollowerRequest;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;


@ElementServiceExport
@ElementEventProducer(
        value = FollowerDao.FOLLOWER_CREATED,
        parameters = Profile.class,
        description = "Called when a follower relationship was created."
)
@ElementEventProducer(
        value = FollowerDao.FOLLOWER_CREATED,
        parameters = {Profile.class, Transaction.class},
        description = "Called when a follower relationship was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = FollowerDao.FOLLOWER_DELETED,
        parameters = Profile.class,
        description = "Called when a follower relationship was deleted."
)
@ElementEventProducer(
        value = FollowerDao.FOLLOWER_DELETED,
        parameters = {Profile.class, Transaction.class},
        description = "Called when a follower relationship was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface FollowerDao {

    String FOLLOWER_CREATED = "dev.getelements.elements.sdk.model.dao.follower.created";

    String FOLLOWER_DELETED = "dev.getelements.elements.sdk.model.dao.follower.deleted";

    /**
     * Fetches all followers for the supplied profile.
     *
     * @param profileId the id of the logged in profile
     * @param offset    the offset
     * @param count     the number of results to return
     * @return a {@link Pagination}
     */
    Pagination<Profile> getFollowersForProfile(String profileId, int offset, int count);

    /**
     * Fetches all followees for the supplied profile.
     *
     * @param profileId the id of the logged in profile
     * @param offset    the offset
     * @param count     the number of results to return
     * @return a {@link Pagination}
     */
    Pagination<Profile> getFolloweesForProfile(String profileId, int offset, int count);

    /**
     * Fetches {@link Profile} instance for the supplied profile id and followed id. Throwing a {@link NotFoundException}
     * if the follower does not exist for the supplied profile id.
     *
     * @param profileId  the id of the logged in profile
     * @param followedId the id of the followed profile
     * @return a {@link Profile}
     */
    Profile getFollowerForProfile(String profileId, String followedId);

    /**
     * Creates a single instance of {@link CreateFollowerRequest} for the supplied profile id and toFollow id.
     *
     * @param profileId         the profile id
     * @param followedProfileId the object to insert into the db
     */
    void createFollowerForProfile(String profileId, String followedProfileId);

    /**
     * Deletes the single instance of {@link CreateFollowerRequest} for the supplied profile id.  Throwing a
     * {@link NotFoundException} if the follower does not exist for the supplied profile id.
     *
     * @param profileId           the id of the logged in profile
     * @param profileToUnfollowId the id of the profile to unfollow
     */
    void deleteFollowerForProfile(String profileId, String profileToUnfollowId);
}
