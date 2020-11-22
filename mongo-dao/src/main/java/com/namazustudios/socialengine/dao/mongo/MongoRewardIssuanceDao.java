package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.client.result.DeleteResult;
import com.namazustudios.socialengine.dao.RewardIssuanceDao;
import com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.ContentionException;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItem;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoRewardIssuance;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoRewardIssuanceId;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.reward.RewardIssuance;
import static com.namazustudios.socialengine.model.reward.RewardIssuance.State;
import static com.namazustudios.socialengine.model.reward.RewardIssuance.State.*;
import static com.namazustudios.socialengine.model.reward.RewardIssuance.Type.*;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.UpdateOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.dozer.Mapper;
import dev.morphia.Datastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.dao.InventoryItemDao.SIMPLE_PRIORITY;
import static com.namazustudios.socialengine.dao.mongo.model.mission.MongoRewardIssuanceId.parseOrThrowNotFoundException;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MongoRewardIssuanceDao implements RewardIssuanceDao {

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoItemDao mongoItemDao;

    private MongoConcurrentUtils mongoConcurrentUtils;

    private ValidationHelper validationHelper;

    @Override
    public RewardIssuance getRewardIssuance(final String id) {
        final MongoRewardIssuance mongoRewardIssuance = getMongoRewardIssuance(id);
        return getDozerMapper().map(mongoRewardIssuance, RewardIssuance.class);
    }

    @Override
    public RewardIssuance getRewardIssuance(final User user, final String context) {
        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final Query<MongoRewardIssuance> query = getDatastore().find(MongoRewardIssuance.class);

        query.filter(Filters.eq("user", mongoUser));
        query.filter(Filters.eq("context", context));

        final List<MongoRewardIssuance> mongoRewardIssuances = query.iterator().toList();

        if (mongoRewardIssuances != null && !mongoRewardIssuances.isEmpty()) {
            final MongoRewardIssuance mongoRewardIssuance = mongoRewardIssuances.get(0);
            return getDozerMapper().map(mongoRewardIssuance, RewardIssuance.class);
        }
        else {
            throw new NotFoundException("Unable to find reward issuance with user: " +
                    mongoUser.getObjectId().toHexString() + "and context: " + context);
        }
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
            final List<State> states,
            final List<String> tags) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final Query<MongoRewardIssuance> query = getDatastore().find(MongoRewardIssuance.class);

        query.filter(Filters.eq("user", mongoUser));

        if (states != null && !states.isEmpty()) {
            query.filter(Filters.in("state", states));
        }

        if (tags != null && !tags.isEmpty()) {
            query.filter(Filters.in("tags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(
            query, offset, count,
            pr -> getDozerMapper().map(pr, RewardIssuance.class), new FindOptions());

    }

    private MongoRewardIssuance getMongoRewardIssuance(final MongoRewardIssuanceId mongoRewardIssuanceId) {
        final MongoRewardIssuance mongoRewardIssuance = getDatastore().find(MongoRewardIssuance.class)
                .filter(Filters.eq("_id", mongoRewardIssuanceId)).first();

        if (mongoRewardIssuance == null) {
            throw new NotFoundException("Mongo reward issuance not found: " + mongoRewardIssuanceId.toHexString());
        }

        return mongoRewardIssuance;
    }

    @Override
    public RewardIssuance getOrCreateRewardIssuance(final RewardIssuance rewardIssuance) {
        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(rewardIssuance.getUser().getId());
        final MongoItem mongoItem = getMongoItemDao().getMongoItemByNameOrId(rewardIssuance.getItem().getId());
        final String context = rewardIssuance.getContext();
        final MongoRewardIssuanceId mongoRewardIssuanceId =
                new MongoRewardIssuanceId(
                        mongoUser.getObjectId(),
                        mongoItem.getObjectId(),
                        rewardIssuance.getItemQuantity(),
                        context
                );

        try {
            final MongoRewardIssuance existingIssuance = getMongoRewardIssuance(mongoRewardIssuanceId);
            return getDozerMapper().map(existingIssuance, RewardIssuance.class);
        }
        catch (NotFoundException e) {

        }

        if (rewardIssuance.getType() == null) {
            rewardIssuance.setType(NON_PERSISTENT);
        }
        rewardIssuance.setState(ISSUED);
        rewardIssuance.setUuid(randomUUID().toString());
        if (rewardIssuance.getType() == PERSISTENT) {
            rewardIssuance.setExpirationTimestamp(null);
        }

        getValidationHelper().validateModel(rewardIssuance, ValidationGroups.Insert.class);
        rewardIssuance.validateTags();

        final MongoRewardIssuance mongoRewardIssuance = getDozerMapper().map(rewardIssuance, MongoRewardIssuance.class);


        mongoRewardIssuance.setObjectId(mongoRewardIssuanceId);

        try {
            getDatastore().insert(mongoRewardIssuance);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        return getDozerMapper().map(mongoRewardIssuance, RewardIssuance.class);

    }

    @Override
    public RewardIssuance updateExpirationTimestamp(RewardIssuance rewardIssuance, long expirationTimestamp) {
        if (expirationTimestamp >= 0) {
            if (REDEEMED.equals(rewardIssuance.getState())) {
                throw new InvalidDataException("Cannot update expirationTimestamp for already-redeemed issuance.");
            }
            if (PERSISTENT.equals(rewardIssuance.getType())) {
                throw new InvalidDataException("Cannot update expirationTimestamp for a PERSISTENT type issuance.");
            }
        }

        final MongoRewardIssuanceId mongoRewardIssuanceId = parseOrThrowNotFoundException(rewardIssuance.getId());

        final Query<MongoRewardIssuance> query = getDatastore().find(MongoRewardIssuance.class);
        query.filter(Filters.eq("_id", mongoRewardIssuanceId));

        if (expirationTimestamp < 0) {
            query.update(UpdateOperators.unset("expirationTimestamp"))
                    .execute(new UpdateOptions().upsert(false));
        }
        else if (expirationTimestamp < currentTimeMillis()) {
            throw new InvalidDataException("expirationTimestamp must be in the future.");
        }
        else {
            query.update(UpdateOperators.set("expirationTimestamp", new Timestamp(expirationTimestamp)))
                    .execute(new UpdateOptions().upsert(false));
        }

        final MongoRewardIssuance mongoRewardIssuance = query.first();

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

        return getDozerMapper().map(inventoryItem, InventoryItem.class);
    }

    private RewardIssuance suspendExpiration(final RewardIssuance rewardIssuance) {
        final RewardIssuance mongoRewardIssuance = updateExpirationTimestamp(rewardIssuance, -1);
        return mongoRewardIssuance;
    }

    private MongoInventoryItem doRedeem(final RewardIssuance rewardIssuance) throws ContentionException {

        final MongoRewardIssuance mongoRewardIssuance = getMongoRewardIssuance(rewardIssuance.getId());

        if (REDEEMED.equals(mongoRewardIssuance.getState())) {
            throw new InvalidDataException("Cannot perform redemption on already-redeemed issuance.");
        }

        final Query<MongoInventoryItem> query = getDatastore().find(MongoInventoryItem.class);

        final MongoUser mongoUser = mongoRewardIssuance.getUser();
        final MongoItem mongoItem = mongoRewardIssuance.getItem();
        final MongoInventoryItemId mongoInventoryItemId = new MongoInventoryItemId(mongoUser, mongoItem, SIMPLE_PRIORITY);

        query.update(UpdateOperators.set("version", randomUUID().toString()))
                .execute(new UpdateOptions().upsert(true));

        query.filter(Filters.eq("_id", mongoInventoryItemId));

        final MongoInventoryItem mongoInventoryItem = query.first();

        if (REDEEMED.equals(mongoRewardIssuance.getState())) {
            return mongoInventoryItem;
        }

        if (mongoInventoryItem == null) {
            query.update(UpdateOperators.set("_id", mongoInventoryItemId),
                    UpdateOperators.set("user", mongoUser),
                    UpdateOperators.set("item", mongoItem),
                    UpdateOperators.set("quantity", rewardIssuance.getItemQuantity()),
                    UpdateOperators.addToSet("rewardIssuanceUuids", mongoRewardIssuance.getUuid()))
                    .execute(new UpdateOptions().upsert(true));
        }
        else {
            query.filter(Filters.eq("version", mongoInventoryItem.getVersion()));

            final boolean add = mongoInventoryItem.getRewardIssuanceUuids() == null   ||
                                mongoInventoryItem.getRewardIssuanceUuids().isEmpty() ||
                                mongoInventoryItem.getRewardIssuanceUuids().stream()
                                    .filter(ri -> ri != null)
                                    .filter(ri -> Objects.equals(mongoRewardIssuance.getUuid(), ri))
                                    .map(ri -> false).findFirst().orElse(true);

            if (add) {
                query.update(UpdateOperators.set("quantity", mongoRewardIssuance.getItemQuantity()),
                        UpdateOperators.set("rewardIssuanceUuids", mongoRewardIssuance.getUuid()))
                        .execute(new UpdateOptions().upsert(true));
            }

        }

        try {
            return query.first();
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

        final MongoRewardIssuanceId mongoRewardIssuanceId = parseOrThrowNotFoundException(rewardIssuance.getId());
        final Query<MongoRewardIssuance> query = getDatastore().find(MongoRewardIssuance.class);
        query.filter(Filters.eq("_id", mongoRewardIssuanceId));

        query.update(UpdateOperators.set("state", REDEEMED))
                .execute(new UpdateOptions().upsert(false));

        final MongoRewardIssuance mongoRewardIssuance = query.first();

        return getDozerMapper().map(mongoRewardIssuance, RewardIssuance.class);
    }


    @Override
    public void delete(String id) {
        final MongoRewardIssuanceId mongoRewardIssuanceId = parseOrThrowNotFoundException(id);
        final DeleteResult deleteResult = getDatastore().find(MongoRewardIssuance.class)
                .filter(Filters.eq("_id", mongoRewardIssuanceId)).delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Reward Issuance not found: " + mongoRewardIssuanceId);
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

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
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

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }
}
