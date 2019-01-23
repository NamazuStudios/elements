package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.RewardIssuanceDao;
import com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.ContentionException;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItem;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoReward;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoRewardIssuance;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoRewardIssuanceId;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.State.*;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.Type.*;
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

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.dao.InventoryItemDao.SIMPLE_PRIORITY;
import static com.namazustudios.socialengine.dao.mongo.model.mission.MongoRewardIssuanceId.parseOrThrowNotFoundException;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MongoRewardIssuanceDao implements RewardIssuanceDao {

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private AdvancedDatastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoRewardDao mongoRewardDao;

    private MongoItemDao mongoItemDao;

    private MongoConcurrentUtils mongoConcurrentUtils;

    private ObjectIndex objectIndex;

    private ValidationHelper validationHelper;

    @Override
    public RewardIssuance getRewardIssuance(final String id) {
        final MongoRewardIssuance mongoRewardIssuance = getMongoRewardIssuance(id);
        return getDozerMapper().map(mongoRewardIssuance, RewardIssuance.class);
    }

    public MongoRewardIssuance getMongoRewardIssuance(final String id) {
        if (isEmpty(nullToEmpty(id).trim())) {
            throw new NotFoundException("Unable to find reward issuance with an id " + id);
        }

        final MongoRewardIssuanceId mongoRewardIssuanceId = parseOrThrowNotFoundException(id);
        return getMongoRewardIssuance(mongoRewardIssuanceId);
    }

    @Override
    public Pagination<RewardIssuance> getRewardIssuances(
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

    private MongoRewardIssuance getMongoRewardIssuance(final MongoRewardIssuanceId mongoRewardIssuanceId) {
        final MongoRewardIssuance mongoRewardIssuance = getDatastore().get(MongoRewardIssuance.class,
                mongoRewardIssuanceId);

        if (mongoRewardIssuance == null) {
            throw new NotFoundException("Mongo reward issuance not found: " + mongoRewardIssuanceId.toHexString());
        }

        return mongoRewardIssuance;
    }

    @Override
    public RewardIssuance createRewardIssuance(final RewardIssuance rewardIssuance) {

        getValidationHelper().validateModel(rewardIssuance, ValidationGroups.Insert.class);

        final MongoRewardIssuance mongoRewardIssuance = getDozerMapper().map(rewardIssuance, MongoRewardIssuance.class);
        if (mongoRewardIssuance.getType() == null) {
            mongoRewardIssuance.setType(NON_PERSISTENT);
        }
        mongoRewardIssuance.setState(ISSUED);
        mongoRewardIssuance.setUuid(randomUUID().toString());
        if (mongoRewardIssuance.getType() == PERSISTENT) {
            mongoRewardIssuance.setExpirationTimestamp(null);
        }

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(rewardIssuance.getUser().getId());
        final MongoReward mongoReward = getMongoRewardDao().getMongoReward(rewardIssuance.getReward().getId());
        final String context = rewardIssuance.getContext();
        final MongoRewardIssuanceId mongoRewardIssuanceId =
                new MongoRewardIssuanceId(mongoUser.getObjectId(), mongoReward.getObjectId(), context);
        mongoRewardIssuance.setObjectId(mongoRewardIssuanceId);

        try {
            getDatastore().insert(mongoRewardIssuance);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        getObjectIndex().index(mongoRewardIssuance);
        return getDozerMapper().map(getDatastore().get(mongoRewardIssuance), RewardIssuance.class);

    }

    @Override
    public RewardIssuance updateExpirationTimestamp(RewardIssuance rewardIssuance, long expirationTimestamp) {
        if (REDEEMED.equals(rewardIssuance.getState())) {
            throw new InvalidDataException("Cannot update expirationTimestamp for already-redeemed issuance.");
        }
        if (PERSISTENT.equals(rewardIssuance.getType())) {
            throw new InvalidDataException("Cannot update expirationTimestamp for a PERSISTENT type issuance.");
        }

        final MongoRewardIssuanceId mongoRewardIssuanceId = parseOrThrowNotFoundException(rewardIssuance.getId());

        final Query<MongoRewardIssuance> query = getDatastore().createQuery(MongoRewardIssuance.class);
        query.field("_id").equal(mongoRewardIssuanceId);

        final UpdateOperations<MongoRewardIssuance> updates = getDatastore().createUpdateOperations(MongoRewardIssuance.class);
        if (expirationTimestamp < 0) {
            updates.unset("expirationTimestamp");
        }
        else if (expirationTimestamp < currentTimeMillis()) {
            throw new InvalidDataException("expirationTimestamp must be in the future.");
        }
        else {
            updates.set("expirationTimestamp", new Timestamp(expirationTimestamp));
        }

        final FindAndModifyOptions options = new FindAndModifyOptions().upsert(false).returnNew(true);
        final MongoRewardIssuance mongoRewardIssuance = getDatastore().findAndModify(query, updates, options);

        return getDozerMapper().map(mongoRewardIssuance, RewardIssuance.class);
    }

    @Override
    public InventoryItem redeem(final RewardIssuance rewardIssuance) {
        MongoInventoryItem inventoryItem;

        try {
            inventoryItem = getMongoConcurrentUtils().performOptimistic(ade -> {
                RewardIssuance issuance = suspendExpiration(rewardIssuance);
                MongoInventoryItem item = doRedeem(issuance);
                issuance = markAsRedeemed(issuance);
                return item;
            });
        } catch (MongoConcurrentUtils.ConflictException ex) {
            throw new TooBusyException(ex);
        }

        if (inventoryItem == null) {
            return null;
        }

        getObjectIndex().index(inventoryItem);

        return getDozerMapper().map(inventoryItem, InventoryItem.class);
    }

    private RewardIssuance suspendExpiration(final RewardIssuance rewardIssuance) {
        final RewardIssuance mongoRewardIssuance = updateExpirationTimestamp(rewardIssuance, -1);
        return mongoRewardIssuance;
    }

    private MongoInventoryItem doRedeem(final RewardIssuance rewardIssuance) throws ContentionException {

        final MongoRewardIssuance mongoRewardIssuance = getMongoRewardIssuance(rewardIssuance.getId());

        final Query<MongoInventoryItem> query = getDatastore().createQuery(MongoInventoryItem.class);
        final UpdateOperations<MongoInventoryItem> updates = getDatastore().createUpdateOperations(MongoInventoryItem.class);

        final MongoUser mongoUser = mongoRewardIssuance.getUser();
        final MongoItem mongoItem = mongoRewardIssuance.getReward().getItem();
        final MongoInventoryItemId mongoInventoryItemId = new MongoInventoryItemId(mongoUser, mongoItem, SIMPLE_PRIORITY);

        updates.set("version", randomUUID().toString());

        query.field("_id").equal(mongoInventoryItemId);

        final MongoInventoryItem mongoInventoryItem = getDatastore().get(MongoInventoryItem.class, mongoInventoryItemId);

        if (REDEEMED.equals(mongoRewardIssuance.getState())) {
            return mongoInventoryItem;
        }

        if (mongoInventoryItem == null) {
            updates.set("_id", mongoInventoryItemId);
            updates.set("user", mongoUser);
            updates.set("item", mongoItem);
            updates.set("quantity", rewardIssuance.getReward().getQuantity());
            updates.addToSet("rewardIssuanceUuids", mongoRewardIssuance.getUuid());
        }
        else {
            query.field("version").equal(mongoInventoryItem.getVersion());

            final boolean add = mongoInventoryItem.getRewardIssuanceUuids() == null   ||
                                mongoInventoryItem.getRewardIssuanceUuids().isEmpty() ||
                                mongoInventoryItem.getRewardIssuanceUuids().stream()
                                    .filter(ri -> ri != null)
                                    .filter(ri -> Objects.equals(mongoRewardIssuance.getUuid(), ri))
                                    .map(ri -> false).findFirst().orElse(true);

            if (add) {
                updates.inc("quantity", mongoRewardIssuance.getReward().getQuantity());
                updates.addToSet("rewardIssuanceUuids", mongoRewardIssuance.getUuid());
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

    private RewardIssuance markAsRedeemed(final RewardIssuance rewardIssuance) {
        if (NON_PERSISTENT.equals(rewardIssuance.getType())) {
            delete(rewardIssuance.getId());
            return null;
        }

        final Query<MongoRewardIssuance> query = getDatastore().createQuery(MongoRewardIssuance.class);
        query.field("_id").equal(rewardIssuance.getId());

        final UpdateOperations<MongoRewardIssuance> updates = getDatastore().createUpdateOperations(MongoRewardIssuance.class);
        updates.set("state", REDEEMED);

        final FindAndModifyOptions options = new FindAndModifyOptions().upsert(false).returnNew(true);
        final MongoRewardIssuance mongoRewardIssuance = getDatastore().findAndModify(query, updates, options);

        return getDozerMapper().map(mongoRewardIssuance, RewardIssuance.class);
    }


    @Override
    public void delete(String id) {
        final MongoRewardIssuanceId mongoRewardIssuanceId = parseOrThrowNotFoundException(id);
        final WriteResult writeResult = getDatastore().delete(MongoRewardIssuance.class, id);

        if (writeResult.getN() == 0) {
            throw new NotFoundException("Pending Reward not found: " + mongoRewardIssuanceId);
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
    public void setMongoRewardDao(MongoRewardDao mongoRewardDao) {
        this.mongoRewardDao = mongoRewardDao;
    }

    public MongoRewardDao getMongoRewardDao() {
        return mongoRewardDao;
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
