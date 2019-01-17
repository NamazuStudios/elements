package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.PendingRewardDao;
import com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.ContentionException;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItem;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoRewardIssuance;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.TooBusyException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.UpdateOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.namazustudios.socialengine.dao.InventoryItemDao.SIMPLE_PRIORITY;
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

    private ValidationHelper validationHelper;

    @Override
    public RewardIssuance getPendingReward(final String id) {
        final MongoRewardIssuance mongoRewardIssuance = getMongoPendingReward(id);
        return getDozerMapper().map(mongoRewardIssuance, RewardIssuance.class);
    }

    public MongoRewardIssuance getMongoPendingReward(final String id) {
        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(id);
        return getMongoPendingReward(objectId);
    }

    @Override
    public Pagination<RewardIssuance> getPendingRewards(
            final User user,
            final int offset, final int count,
            final Set<RewardIssuance.State> states) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final Query<MongoRewardIssuance> query = getDatastore().createQuery(MongoRewardIssuance.class);

        query.field("user").equal(mongoUser);

        if (states != null && !states.isEmpty()) {
            query.field("state").hasAnyOf(states);
        }

        return getMongoDBUtils().paginationFromQuery(
            query, offset, count,
            pr -> getDozerMapper().map(pr, RewardIssuance.class));

    }

    private MongoRewardIssuance getMongoPendingReward(final ObjectId objectId) {

        final MongoRewardIssuance mongoRewardIssuance = getDatastore().get(MongoRewardIssuance.class, objectId);

        if (mongoRewardIssuance == null) {
            throw new NotFoundException("Mongo pending reward not found: " + objectId.toHexString());
        }

        return mongoRewardIssuance;

    }

    @Override
    public RewardIssuance createPendingReward(final RewardIssuance rewardIssuance) {

        getValidationHelper().validateModel(rewardIssuance, ValidationGroups.Insert.class);

        final MongoRewardIssuance mongoRewardIssuance = getDozerMapper().map(rewardIssuance, MongoRewardIssuance.class);
        mongoRewardIssuance.setState(CREATED);
        mongoRewardIssuance.setExpires(new Timestamp(currentTimeMillis()));

        try {
            getDatastore().insert(mongoRewardIssuance);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        getObjectIndex().index(mongoRewardIssuance);
        return getDozerMapper().map(getDatastore().get(mongoRewardIssuance), RewardIssuance.class);

    }

    @Override
    public RewardIssuance flagPending(final RewardIssuance rewardIssuance) {

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(rewardIssuance.getId());

        final Query<MongoRewardIssuance> query = getDatastore().createQuery(MongoRewardIssuance.class);
        query.field("_id").equal(objectId);
        query.field("state").equal(CREATED);

        final UpdateOperations<MongoRewardIssuance> updates = getDatastore().createUpdateOperations(MongoRewardIssuance.class);
        updates.unset("expires");
        updates.set("state", PENDING);

        final FindAndModifyOptions options = new FindAndModifyOptions().upsert(false).returnNew(true);
        final MongoRewardIssuance mongoRewardIssuance = getDatastore().findAndModify(query, updates, options);

        return getDozerMapper().map(mongoRewardIssuance, RewardIssuance.class);

    }

    @Override
    public InventoryItem redeem(final RewardIssuance reward) {

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

    private MongoInventoryItem doRedeem(final RewardIssuance reward) throws ContentionException {

        final MongoRewardIssuance mongoRewardIssuance = getMongoPendingReward(reward.getId());

        final Query<MongoInventoryItem> query = getDatastore().createQuery(MongoInventoryItem.class);
        final UpdateOperations<MongoInventoryItem> updates = getDatastore().createUpdateOperations(MongoInventoryItem.class);

        final MongoUser mongoUser = mongoRewardIssuance.getUser();
        final MongoItem mongoItem = mongoRewardIssuance.getReward().getItem();
        final MongoInventoryItemId mongoInventoryItemId = new MongoInventoryItemId(mongoUser, mongoItem, SIMPLE_PRIORITY);

        final MongoInventoryItem mongoInventoryItem = getDatastore().get(MongoInventoryItem.class, mongoInventoryItemId);
        updates.set("version", randomUUID().toString());

        query.field("_id").equal(mongoInventoryItemId);

        if (!PENDING.equals(mongoRewardIssuance.getState())) {
            return getDatastore().get(MongoInventoryItem.class, mongoInventoryItemId);
        } else if (mongoInventoryItem == null) {
            updates.set("_id", mongoInventoryItemId);
            updates.set("user", mongoUser);
            updates.set("item", mongoItem);
            updates.set("quantity", reward.getReward().getQuantity());
            updates.addToSet("pendingRewards", mongoRewardIssuance);
        } else {

            query.field("version").equal(mongoInventoryItem.getVersion());

            final boolean add = mongoInventoryItem.getPendingRewards() == null   ||
                                mongoInventoryItem.getPendingRewards().isEmpty() ||
                                mongoInventoryItem.getPendingRewards().stream()
                                    .filter(pr -> pr != null)
                                    .filter(pr -> Objects.equals(mongoRewardIssuance.getObjectId(), pr.getObjectId()))
                                    .map(pr -> false).findFirst().orElse(true);

            if (add) {
                updates.inc("quantity", mongoRewardIssuance.getReward().getQuantity());
                updates.addToSet("pendingRewards", mongoRewardIssuance);
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

        final Query<MongoRewardIssuance> query = getDatastore().createQuery(MongoRewardIssuance.class);
        query.field("_id").in(objectIds);
        query.field("state").equal(PENDING);

        final List<MongoRewardIssuance> flaggedPendingRewards = new ArrayList<>(query.asList());
        if (flaggedPendingRewards.isEmpty()) return inventoryItem;

        final UpdateOperations<MongoRewardIssuance> updates = getDatastore().createUpdateOperations(MongoRewardIssuance.class);
        updates.set("state", REWARDED);
        updates.set("expires", new Timestamp(currentTimeMillis()));
        getDatastore().update(query, updates, new UpdateOptions().multi(true));

        final UpdateOperations<MongoInventoryItem> inventoryItemUpdates = getDatastore().createUpdateOperations(MongoInventoryItem.class);
        inventoryItemUpdates.removeAll("pendingRewards", flaggedPendingRewards);

        return getDatastore().get(inventoryItem);

    }


    @Override
    public void delete(String id) {

        final ObjectId pendingRewardId = getMongoDBUtils().parseOrThrowNotFoundException(id);
        final WriteResult writeResult = getDatastore().delete(MongoRewardIssuance.class, id);

        if (writeResult.getN() == 0) {
            throw new NotFoundException("Pending Reward not found: " + pendingRewardId);
        }

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

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }
}
