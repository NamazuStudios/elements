package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.PendingRewardDao;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoPendingReward;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.PendingReward;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;

public class MongoPendingRewardDao implements PendingRewardDao {

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public PendingReward getPendingReward(final String id) {
        final MongoPendingReward mongoPendingReward = getMongoPendingReward(id);
        return getDozerMapper().map(mongoPendingReward, PendingReward.class);
    }

    public MongoPendingReward getMongoPendingReward(final String id) {
        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(id);
        return getMongoPendingReward(objectId);
    }

    private MongoPendingReward getMongoPendingReward(final ObjectId objectId) {
        return null;
    }

    @Override
    public InventoryItem redeem(final PendingReward reward) {

        return null;
    }

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

}
