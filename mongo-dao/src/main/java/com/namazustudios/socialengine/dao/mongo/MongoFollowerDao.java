package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.WriteResult;
import com.namazustudios.socialengine.dao.FollowerDao;
import com.namazustudios.socialengine.dao.mongo.model.*;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.follower.CreateFollowerRequest;
import com.namazustudios.socialengine.model.profile.Profile;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;

import static java.lang.String.format;

public class MongoFollowerDao implements FollowerDao {
    private AdvancedDatastore datastore;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    private MongoProfileDao mongoProfileDao;

    @Override
    public Pagination<Profile> getFollowersForProfile(String profileId, int offset, int count) {
        final Query<MongoFollower> followerQuery = getDatastore().createQuery(MongoFollower.class);
        final ObjectId profileOid = getMongoDBUtils().parseOrReturnNull(profileId);

        followerQuery.field("_id.profileId").equal(profileOid);

        return getMongoDBUtils().paginationFromQuery(followerQuery, offset, count, f -> getDozerMapper().map(f.getFollowedProfile(), Profile.class));
    }

    @Override
    public Profile getFollowerForProfile(String profileId, String followedId) {
        final Query<MongoFollower> followerQuery = getDatastore().createQuery(MongoFollower.class);
        final MongoFollowerId id = new MongoFollowerId(profileId, followedId);

        followerQuery.field("_id").equal(id);

        MongoFollower follower = followerQuery.get();

        if(follower == null) {
            throw new NotFoundException(format("No follower relationship exists with profile id %s, and followed id %s", profileId, followedId));
        }

        return getDozerMapper().map(follower.getFollowedProfile(), Profile.class);
    }

    @Override
    public void createFollowerForProfile(String profileId, CreateFollowerRequest createFollowerRequest) {
        final Query<MongoFollower> followerQuery = getDatastore().createQuery(MongoFollower.class);
        final MongoFollowerId id = new MongoFollowerId(profileId, createFollowerRequest.getFollowedId());

        followerQuery.field("_id").equal(id);

        final UpdateOperations<MongoFollower> updateOperations = getDatastore().createUpdateOperations(MongoFollower.class);
        updateOperations.set("_id", id);
        updateOperations.set("followedProfile", getDatastore().get(MongoProfile.class, id.getFollowedId()));

        getDatastore().findAndModify(followerQuery, updateOperations, new FindAndModifyOptions().upsert(true));
    }

    @Override
    public void deleteFollowerForProfile(String profileId, String profileToUnfollowId) {
        final Query<MongoFollower> followerQuery = getDatastore().createQuery(MongoFollower.class);
        final MongoFollowerId id = new MongoFollowerId(profileId, profileToUnfollowId);

        followerQuery.field("_id").equal(id);

        final WriteResult writeResult = getDatastore().delete(followerQuery);

        if (writeResult.getN() == 0) {
            throw new NotFoundException("Follower not found: " + profileToUnfollowId);
        } else if (writeResult.getN() > 1) {
            throw new InternalException("Deleted more rows than expected.");
        }
    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
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
