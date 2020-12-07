package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.FollowerDao;
import com.namazustudios.socialengine.dao.mongo.model.*;
import com.namazustudios.socialengine.exception.FriendNotFoundException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.follower.Follower;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class MongoFollowerDao implements FollowerDao {
    private AdvancedDatastore datastore;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    private MongoProfileDao mongoProfileDao;

    @Override
    public Pagination<Profile> getFollowersForProfile(String profileId, int offset, int count) {
        final Query<MongoFollower> followerQuery = getDatastore().createQuery(MongoFollower.class);

        followerQuery.field("profileId").equal(profileId);

        List<ObjectId> followerIds = followerQuery.asList().stream().map(f -> {
            return getMongoDBUtils().parseOrReturnNull(f.getFollowedId());
        }).collect(Collectors.toList());

        return getMongoProfileDao().getActiveProfiles(followerIds, offset, count);
    }

    @Override
    public Profile getFollowerForProfile(String profileId, String followedId) {
        final Query<MongoFollower> followerQuery = getDatastore().createQuery(MongoFollower.class);

        followerQuery.and(
                followerQuery.criteria("profileId").equal(profileId),
                followerQuery.criteria("followedId").equal(followedId)
        );

        MongoFollower follower = followerQuery.get();

        if(follower == null) {
            throw new NotFoundException(format("No follower relationship exists with profile id %s, and followed id %s", profileId, followedId));
        }

        return getMongoProfileDao().getActiveProfile(follower.getFollowedId());
    }

    @Override
    public void createFollowerForProfile(Follower follower) {
        final Query<MongoFollower> followerQuery = getDatastore().createQuery(MongoFollower.class);

        followerQuery.and(
                followerQuery.criteria("profileId").equal(follower.getProfileId()),
                followerQuery.criteria("followedId").equal(follower.getFollowedId())
        );

        final UpdateOperations<MongoFollower> updateOperations = getDatastore().createUpdateOperations(MongoFollower.class);
        updateOperations.set("profileId", follower.getProfileId());
        updateOperations.set("followedId", follower.getFollowedId());

        getDatastore().findAndModify(followerQuery, updateOperations, new FindAndModifyOptions().upsert(true));
    }

    @Override
    public void deleteFollowerForProfile(String profileId, String profileToUnfollowId) {
        final Query<MongoFollower> followerQuery = getDatastore().createQuery(MongoFollower.class);

        followerQuery.and(
                followerQuery.criteria("profileId").equal(profileId),
                followerQuery.criteria("followedId").equal(profileToUnfollowId)
        );

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
