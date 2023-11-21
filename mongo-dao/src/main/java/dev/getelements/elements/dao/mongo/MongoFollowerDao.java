package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.FollowerDao;
import dev.getelements.elements.dao.mongo.model.MongoFollower;
import dev.getelements.elements.dao.mongo.model.MongoFollowerId;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.follower.CreateFollowerRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.morphia.Datastore;
import org.dozer.Mapper;

import javax.inject.Inject;

import static dev.morphia.query.filters.Filters.eq;
import static java.lang.String.format;

public class MongoFollowerDao implements FollowerDao {

    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    private MongoProfileDao mongoProfileDao;

    @Override
    public Pagination<Profile> getFollowersForProfile(final String profileId,
                                                      final int offset,
                                                      final int count) {

        final var query = getDatastore().find(MongoFollower.class);
        final var profileObjectId = getMongoDBUtils().parseOrThrow(profileId, ProfileNotFoundException::new);

        query.filter(eq("_id.profileId", profileObjectId));

        return getMongoDBUtils()
                .paginationFromQuery(
                    query, offset, count,
                    f -> getDozerMapper().map(f.getFollowedProfile(), Profile.class)
                );

    }

    @Override
    public Pagination<Profile> getFolloweesForProfile(final String profileId,
                                                      final int offset,
                                                      final int count) {

        final var query = getDatastore().find(MongoFollower.class);
        final var profileObjectId = getMongoDBUtils().parseOrThrow(profileId, ProfileNotFoundException::new);

        query.filter(eq("_id.followedId", profileObjectId));

        return getMongoDBUtils()
                .paginationFromQuery(
                        query, offset, count,
                        f -> getDozerMapper().map(f.getProfile(), Profile.class)
                );

    }

    @Override
    public Profile getFollowerForProfile(final String profileId,
                                         final String followedId) {

        final var followerQuery = getDatastore().find(MongoFollower.class);
        final var mongoFollowerId = new MongoFollowerId(profileId, followedId);

        followerQuery.filter(eq("_id", mongoFollowerId));

        final var follower = followerQuery.first();

        if(follower == null) {

            final var msg = format("No follower relationship exists with profile id %s, and followed id %s",
                profileId,
                followedId);

            throw new NotFoundException(msg);

        }

        return getDozerMapper().map(follower.getFollowedProfile(), Profile.class);

    }

    @Override
    public void createFollowerForProfile(final String profileId,
                                         final CreateFollowerRequest createFollowerRequest) {

        final var follower = getMongoProfileDao().getActiveMongoProfile(profileId);
        final var followed = getMongoProfileDao().getActiveMongoProfile(createFollowerRequest.getFollowedId());

        final var mongoFollower = new MongoFollower();
        final var mongoFollowerId = new MongoFollowerId(follower.getObjectId(), followed.getObjectId());

        mongoFollower.setObjectId(mongoFollowerId);
        mongoFollower.setFollowedProfile(followed);

        getMongoDBUtils().performV(ds -> getDatastore().insert(mongoFollower));

    }

    @Override
    public void deleteFollowerForProfile(final String profileId,
                                         final String profileToUnfollowId) {

        final var query = getDatastore().find(MongoFollower.class);
        final var mongoFollowerId = new MongoFollowerId(profileId, profileToUnfollowId);

        query.filter(eq("_id", mongoFollowerId));

        final var result = query.delete();

        if (result.getDeletedCount() == 0) {
            throw new NotFoundException("Follower not found: " + profileToUnfollowId);
        } else if (result.getDeletedCount() > 1) {
            throw new InternalException("Deleted more rows than expected.");
        }

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }

}
