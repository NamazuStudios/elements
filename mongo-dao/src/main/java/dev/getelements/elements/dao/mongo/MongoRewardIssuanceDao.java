package dev.getelements.elements.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.mongo.goods.MongoItemDao;
import dev.getelements.elements.sdk.dao.RewardIssuanceDao;
import dev.getelements.elements.dao.mongo.MongoConcurrentUtils.ContentionException;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.dao.mongo.model.goods.MongoInventoryItem;
import dev.getelements.elements.dao.mongo.model.goods.MongoInventoryItemId;
import dev.getelements.elements.dao.mongo.model.mission.MongoRewardIssuance;
import dev.getelements.elements.dao.mongo.model.mission.MongoRewardIssuanceId;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.*;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.dao.mongo.model.mission.MongoRewardIssuanceId.parseOrThrowNotFoundException;
import static dev.getelements.elements.sdk.dao.InventoryItemDao.SIMPLE_PRIORITY;
import static dev.getelements.elements.sdk.model.goods.ItemCategory.FUNGIBLE;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.State;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.State.ISSUED;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.State.REDEEMED;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.Type.NON_PERSISTENT;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.Type.PERSISTENT;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.*;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;

public class MongoRewardIssuanceDao implements RewardIssuanceDao {

    private static final Logger logger = LoggerFactory.getLogger(MongoRewardIssuanceDao.class);

    private MapperRegistry dozerMapperRegistry;

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
        final MongoUser mongoUser = getMongoUserDao().getMongoUser(user.getId());
        final Query<MongoRewardIssuance> query = getDatastore().find(MongoRewardIssuance.class);

        query.filter(eq("user", mongoUser));
        query.filter(eq("context", context));

        final List<MongoRewardIssuance> mongoRewardIssuances;

        try (var iterator = query.iterator()) {
            mongoRewardIssuances = iterator.toList();
        }

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

        if (nullToEmpty(id).isBlank()) {
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

        final MongoUser mongoUser = getMongoUserDao().getMongoUser(user.getId());
        final Query<MongoRewardIssuance> query = getDatastore().find(MongoRewardIssuance.class);

        query.filter(eq("user", mongoUser));

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
                .filter(eq("_id", mongoRewardIssuanceId)).first();

        if (mongoRewardIssuance == null) {
            throw new NotFoundException("Mongo reward issuance not found: " + mongoRewardIssuanceId.toHexString());
        }

        return mongoRewardIssuance;
    }

    @Override
    public RewardIssuance getOrCreateRewardIssuance(final RewardIssuance rewardIssuance) {

        final var mongoUser = getMongoUserDao().getMongoUser(rewardIssuance.getUser().getId());
        final var mongoItem = getMongoItemDao().getMongoItemByNameOrId(rewardIssuance.getItem().getId());
        final var mongoItemCategory = mongoItem.getCategory();

        if (!FUNGIBLE.equals(mongoItemCategory)) {
            throw new InternalException("Rewards only support fungible items.");
        }

        final var context = rewardIssuance.getContext();

        final var mongoRewardIssuanceId =
            new MongoRewardIssuanceId(
                mongoUser.getObjectId(),
                mongoItem.getObjectId(),
                rewardIssuance.getItemQuantity(),
                context
            );

        try {
            final var existingIssuance = getMongoRewardIssuance(mongoRewardIssuanceId);
            return getDozerMapper().map(existingIssuance, RewardIssuance.class);
        } catch (NotFoundException e) {
            logger.trace("Isusance not found.", e);
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

        final var mongoRewardIssuance = getDozerMapper().map(rewardIssuance, MongoRewardIssuance.class);


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

        final var mongoRewardIssuanceId = parseOrThrowNotFoundException(rewardIssuance.getId());
        final var query = getDatastore().find(MongoRewardIssuance.class).filter(eq("_id", mongoRewardIssuanceId));

        final var builder = new UpdateBuilder();

        if (expirationTimestamp < 0) {
            builder.with(unset("expirationTimestamp"));
        } else if (expirationTimestamp < currentTimeMillis()) {
            throw new InvalidDataException("expirationTimestamp must be in the future.");
        } else {
            final var now = new Timestamp(expirationTimestamp);
            builder.with(set("expirationTimestamp", now));
        }

        final var opts = new ModifyOptions().upsert(false).returnDocument(AFTER);
        final var mongoRewardIssuance = builder.execute(query, opts);

        return getDozerMapper().map(mongoRewardIssuance, RewardIssuance.class);
    }

    @Override
    public InventoryItem redeem(final RewardIssuance rewardIssuance) {
        MongoInventoryItem inventoryItem;

        try {
            inventoryItem = getMongoConcurrentUtils().performOptimistic(ade -> {
                final var issuance = suspendExpiration(rewardIssuance);
                final var item = doRedeem(issuance);
                markAsRedeemed(issuance);
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
        return updateExpirationTimestamp(rewardIssuance, -1);
    }

    private MongoInventoryItem doRedeem(final RewardIssuance rewardIssuance) throws ContentionException {

        final var mongoRewardIssuance = getMongoRewardIssuance(rewardIssuance.getId());

        if (REDEEMED.equals(mongoRewardIssuance.getState())) {
            throw new InvalidDataException("Cannot perform redemption on already-redeemed issuance.");
        }

        final var query = getDatastore().find(MongoInventoryItem.class);

        final var mongoUser = mongoRewardIssuance.getUser();
        final var mongoItem = mongoRewardIssuance.getItem();
        final var mongoInventoryItemId = new MongoInventoryItemId(mongoUser, mongoItem, SIMPLE_PRIORITY);

        query.filter(eq("_id", mongoInventoryItemId));

        final var mongoInventoryItem = query.first();

        if (REDEEMED.equals(mongoRewardIssuance.getState())) {
            return mongoInventoryItem;
        }

        final var builder = new UpdateBuilder();

        if (mongoInventoryItem == null) {
            builder.with(
                set("_id", mongoInventoryItemId),
                set("user", mongoUser),
                set("item", mongoItem),
                set("quantity", rewardIssuance.getItemQuantity()),
                set("version", randomUUID().toString()),
                addToSet("rewardIssuanceUuids", mongoRewardIssuance.getUuid())
            );
        } else {

            final var add = mongoInventoryItem.getRewardIssuanceUuids() == null   ||
                            mongoInventoryItem.getRewardIssuanceUuids().isEmpty() ||
                            mongoInventoryItem.getRewardIssuanceUuids().stream()
                                .filter(Objects::nonNull)
                                .filter(ri -> Objects.equals(mongoRewardIssuance.getUuid(), ri))
                                .map(ri -> false).findFirst().orElse(true);

            query.filter(eq("version", mongoInventoryItem.getVersion()));

            if (add) {
                builder.with(
                    inc("quantity", mongoRewardIssuance.getItemQuantity()),
                    addToSet("rewardIssuanceUuids", mongoRewardIssuance.getUuid())
                );
            }

        }

        final var opts = new ModifyOptions().upsert(true).returnDocument(AFTER);
        final var result = builder.execute(query, opts);

        if (result == null) {
            throw new ContentionException();
        }

        return result;
    }

    private RewardIssuance markAsRedeemed(final RewardIssuance rewardIssuance) {

        if (NON_PERSISTENT.equals(rewardIssuance.getType())) {
            delete(rewardIssuance.getId());
            return null;
        }

        final var mongoRewardIssuanceId = parseOrThrowNotFoundException(rewardIssuance.getId());
        final var query = getDatastore().find(MongoRewardIssuance.class);
        query.filter(eq("_id", mongoRewardIssuanceId));

        final var mongoRewardIssuance = getMongoDBUtils().perform(ds ->
            query.modify(set("state", REDEEMED))
                 .execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        return getDozerMapper().map(mongoRewardIssuance, RewardIssuance.class);

    }

    @Override
    public void delete(String id) {
        final MongoRewardIssuanceId mongoRewardIssuanceId = parseOrThrowNotFoundException(id);
        final DeleteResult deleteResult = getDatastore().find(MongoRewardIssuance.class)
                .filter(eq("_id", mongoRewardIssuanceId)).delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Reward Issuance not found: " + mongoRewardIssuanceId);
        }
    }

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
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
