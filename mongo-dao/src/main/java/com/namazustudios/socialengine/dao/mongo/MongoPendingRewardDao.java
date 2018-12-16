package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.elements.fts.QueryExecutor;
import com.namazustudios.socialengine.dao.PendingRewardDao;
import com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.ContentionException;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItem;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoPendingReward;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.TooBusyException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.PendingReward;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.*;

import static com.namazustudios.socialengine.dao.InventoryItemDao.SIMPLE_PRIORITY;
import static com.namazustudios.socialengine.dao.mongo.model.mission.MongoPendingReward.State.PENDING;
import static com.namazustudios.socialengine.dao.mongo.model.mission.MongoPendingReward.State.REWARDED;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;

public class MongoPendingRewardDao implements PendingRewardDao {

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private AdvancedDatastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoItemDao mongoItemDao;

    private MongoConcurrentUtils mongoConcurrentUtils;

    private ObjectIndex objectIndex;

    @Override
    public PendingReward getPendingReward(final String id) {
        final MongoPendingReward mongoPendingReward = getMongoPendingReward(id);
        return getDozerMapper().map(mongoPendingReward, PendingReward.class);
    }

    public MongoPendingReward getMongoPendingReward(final String id) {
        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(id);
        return getMongoPendingReward(objectId);
    }

    @Override
    public Pagination<PendingReward> getPendingRewards(
            final User user,
            final int offset, final int count,
            final Set<PendingReward.State> states) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final Query<MongoPendingReward> query = getDatastore().createQuery(MongoPendingReward.class);

        query.field("user").equal(mongoUser);

        if (states != null && !states.isEmpty()) {
            query.field("state").hasAnyOf(states);
        }

        return getMongoDBUtils().paginationFromQuery(
            query, offset, count,
            pr -> getDozerMapper().map(pr, PendingReward.class));

    }

    private MongoPendingReward getMongoPendingReward(final ObjectId objectId) {

        final MongoPendingReward mongoPendingReward = getDatastore().get(MongoPendingReward.class, objectId);

        if (mongoPendingReward == null) {
            throw new NotFoundException("Mongo pending reward not found: " + objectId.toHexString());
        }

        return mongoPendingReward;

    }

    @Override
    public InventoryItem redeem(final PendingReward reward) {

        MongoInventoryItem inventoryItem;

        try {
            inventoryItem = getMongoConcurrentUtils().performOptimistic(ade -> doRedeem(reward));
        } catch (MongoConcurrentUtils.ConflictException ex) {
            throw new TooBusyException(ex);
        }

        if (inventoryItem == null) {
            return null;
        }

        inventoryItem = vacuumPendingRewards(inventoryItem);
        getObjectIndex().index(inventoryItem);

        return getDozerMapper().map(inventoryItem, InventoryItem.class);

    }

    private MongoInventoryItem doRedeem(final PendingReward reward) throws ContentionException {

        final MongoPendingReward mongoPendingReward = getMongoPendingReward(reward.getId());

        final Query<MongoInventoryItem> query = getDatastore().createQuery(MongoInventoryItem.class);
        final UpdateOperations<MongoInventoryItem> updates = getDatastore().createUpdateOperations(MongoInventoryItem.class);

        final MongoUser mongoUser = mongoPendingReward.getUser();
        final MongoItem mongoItem = mongoPendingReward.getReward().getItem();
        final MongoInventoryItemId mongoInventoryItemId = new MongoInventoryItemId(mongoUser, mongoItem, SIMPLE_PRIORITY);

        final MongoInventoryItem mongoInventoryItem = getDatastore().get(MongoInventoryItem.class, mongoInventoryItemId);
        updates.set("version", randomUUID().toString());

        if (!PENDING.equals(mongoPendingReward.getState())) {
            return getDatastore().get(MongoInventoryItem.class, mongoInventoryItemId);
        } else if (mongoInventoryItem == null) {
            updates.set("_id", mongoInventoryItemId);
            updates.set("user", mongoUser);
            updates.set("item", mongoItem);
            updates.set("quantity", reward.getReward().getQuantity());
            updates.max("quantity", 0);
            updates.addToSet("pendingRewards", mongoPendingReward);
        } else {

            query.field("_id").equal(mongoInventoryItemId);
            query.field("version").equal(mongoInventoryItem.getVersion());

            final boolean add = mongoInventoryItem.getPendingRewards() == null   ||
                                mongoInventoryItem.getPendingRewards().isEmpty() ||
                                mongoInventoryItem.getPendingRewards().stream()
                                    .filter(pr -> pr != null)
                                    .filter(pr -> Objects.equals(mongoInventoryItem.getObjectId(), pr.getObjectId()))
                                    .map(pr -> false).findFirst().orElse(true);

            if (!add) {
                updates.inc("quantity", mongoPendingReward.getReward().getQuantity());
                updates.max("quantity", 0);
                updates.addToSet("pendingRewards", mongoPendingReward);
            }

        }

        try {
            return getDatastore().findAndModify(query, updates, new FindAndModifyOptions()
                    .upsert(true)
                    .returnNew(true));
        } catch (MongoException ex) {
            if (ex.getCode() == 11000) {
                throw new ContentionException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

    }

    private MongoInventoryItem vacuumPendingRewards(final MongoInventoryItem inventoryItem) {

        final Set<ObjectId> objectIds =
            inventoryItem.getPendingRewards() == null ?   emptySet() :
            inventoryItem.getPendingRewards().isEmpty() ? emptySet() :
            inventoryItem.getPendingRewards().stream().map(pr -> pr.getObjectId()).collect(toSet());

        final Query<PendingReward> query = getDatastore().createQuery(PendingReward.class);
        query.field("_id").in(objectIds);

        final List<PendingReward> flaggedPendingRewards = new ArrayList<>(query.asList());

        final UpdateOperations<PendingReward> rewardUpdates = getDatastore().createUpdateOperations(PendingReward.class);
        rewardUpdates.set("state", REWARDED);
        rewardUpdates.set("expiry", new Timestamp(currentTimeMillis()));
        getDatastore().findAndModify(query, rewardUpdates);

        final UpdateOperations<MongoInventoryItem> inventoryItemUpdates = getDatastore().createUpdateOperations(MongoInventoryItem.class);
        inventoryItemUpdates.removeAll("pendingRewards", flaggedPendingRewards);

        return getDatastore().get(inventoryItem);

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

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoItemDao getMongoItemDao() {
        return mongoItemDao;
    }

    @Inject
    public void setMongoItemDao(MongoItemDao mongoItemDao) {
        this.mongoItemDao = mongoItemDao;
    }

    public MongoConcurrentUtils getMongoConcurrentUtils() {
        return mongoConcurrentUtils;
    }

    @Inject
    public void setMongoConcurrentUtils(MongoConcurrentUtils mongoConcurrentUtils) {
        this.mongoConcurrentUtils = mongoConcurrentUtils;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }
}
