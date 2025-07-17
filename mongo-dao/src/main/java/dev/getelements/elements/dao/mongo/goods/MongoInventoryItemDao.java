package dev.getelements.elements.dao.mongo.goods;

import com.mongodb.WriteConcern;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.sdk.dao.InventoryItemDao;
import dev.getelements.elements.dao.mongo.MongoConcurrentUtils;
import dev.getelements.elements.dao.mongo.MongoConcurrentUtils.ContentionException;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.dao.mongo.model.goods.MongoInventoryItem;
import dev.getelements.elements.dao.mongo.model.goods.MongoInventoryItemId;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.TooBusyException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import jakarta.inject.Inject;

import java.util.List;

import static dev.getelements.elements.dao.mongo.model.goods.MongoInventoryItemId.parseOrThrowNotFoundException;
import static dev.getelements.elements.sdk.model.goods.ItemCategory.FUNGIBLE;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.in;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.lang.Integer.max;
import static java.util.UUID.randomUUID;

public class MongoInventoryItemDao implements InventoryItemDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MapperRegistry dozerMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    private MongoItemDao mongoItemDao;

    private MongoUserDao mongoUserDao;

    private MongoConcurrentUtils mongoConcurrentUtils;

    @Override
    public InventoryItem getInventoryItem(final String inventoryItemId) {

        final MongoInventoryItemId objectId = parseOrThrowNotFoundException(inventoryItemId);
        final Query<MongoInventoryItem> query = getDatastore().find(MongoInventoryItem.class);

        query.filter(eq("_id", objectId));

        final MongoInventoryItem item = query.first();

        if (item == null) {
            throw new NotFoundException("Unable to find item with an id of " + inventoryItemId);
        }

        return getDozerMapper().map(item, InventoryItem.class);

    }

    @Override
    public InventoryItem getInventoryItemByItemNameOrId(final User user, final String itemNameOrId, int priority) {

        final var mongoUser = getMongoUserDao().getMongoUser(user.getId());
        final var mongoItem = getMongoItemDao().getMongoItemByNameOrId(itemNameOrId);
        final var objectId = new MongoInventoryItemId(mongoUser, mongoItem, priority);

        final MongoInventoryItem item = getDatastore()
            .find(MongoInventoryItem.class)
            .filter(eq("_id", objectId))
            .first();

        if (item == null) {
            throw new NotFoundException(
                "Unable to find item with an id of " + itemNameOrId +
                " for user " + user.getId() +
                " and priority " + priority + "."
            );
        }

        return getDozerMapper().map(item, InventoryItem.class);

    }

    @Override
    public Pagination<InventoryItem> getUserPublicInventoryItems(final int offset, final int count, User user) {

        List<MongoItem> publicItems = mongoItemDao.getPublicItems();

        final var query = getDatastore()
                .find(MongoInventoryItem.class)
                .filter(in("item", publicItems))
                .filter(eq("user", getDozerMapper().map(user, MongoUser.class)));

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoItem -> getDozerMapper().map(mongoItem, InventoryItem.class), new FindOptions());

    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset, final int count, final User user,
                                                       final String search) {

        final var query = getDatastore()
            .find(MongoInventoryItem.class)
            .filter(eq("user", getDozerMapper().map(user, MongoUser.class)));

        return getMongoDBUtils().paginationFromQuery(
            query, offset, count,
            mongoItem -> getDozerMapper().map(mongoItem, InventoryItem.class), new FindOptions());
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset, final int count) {

        final var query = getDatastore().find(MongoInventoryItem.class);

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoItem -> getDozerMapper().map(mongoItem, InventoryItem.class), new FindOptions());

    }

    @Override
    public InventoryItem createInventoryItem(final InventoryItem inventoryItem) {

        getValidationHelper().validateModel(inventoryItem, Insert.class);

        normalize(inventoryItem);

        final var mongoItem = getMongoItemDao().getMongoItem(inventoryItem.getItem());

        final var mongoItemCategory = mongoItem.getCategory();

        if (mongoItemCategory != null && !FUNGIBLE.equals(mongoItemCategory)) {
            throw new InvalidDataException("Item must be of type: " + FUNGIBLE);
        }

        final var mongoUser = getMongoUserDao().getMongoUser(inventoryItem.getUser().getId());

        final MongoInventoryItem mongoInventoryItem = getDozerMapper().map(inventoryItem, MongoInventoryItem.class);

        mongoInventoryItem.setVersion(randomUUID().toString());
        mongoInventoryItem.setObjectId(new MongoInventoryItemId(mongoUser, mongoItem, inventoryItem.getPriority()));

        getMongoDBUtils().performV(ds -> ds.insert(mongoInventoryItem));

        final Query<MongoInventoryItem> query = getDatastore().find(MongoInventoryItem.class);
        query.filter(eq("_id", mongoInventoryItem.getObjectId()));

        return getDozerMapper().map(query.first(), InventoryItem.class);

    }

    @Override
    public InventoryItem updateInventoryItem(final String inventoryItemId, int quantity) {

        getValidationHelper().validateModel(inventoryItemId, Update.class);

        final var query = getDatastore().find(MongoInventoryItem.class);
        final var objectId = parseOrThrowNotFoundException(inventoryItemId);
        query.filter(eq("_id", objectId));

        final var mongoInventoryItem = getMongoDBUtils().perform(ds ->
            query.modify(
                set("version", randomUUID().toString()),
                set("quantity", quantity)
            ).execute(new ModifyOptions().upsert(true).returnDocument(ReturnDocument.AFTER))
        );

        if (mongoInventoryItem == null) {
            throw new NotFoundException("Inventory item with id of " + inventoryItemId + " does not exist");
        }

        return getDozerMapper().map(mongoInventoryItem, InventoryItem.class);

    }

    @Override
    public InventoryItem setQuantityForItem(
            final User user,
            final String itemNameOrId,
            final int priority,
            final int quantity) {

        if (quantity < 0) throw new IllegalArgumentException("invalid quantity: " + quantity);

        final var mongoUser = getMongoUserDao().getMongoUser(user.getId());
        final var mongoItem = getMongoItemDao().getMongoItemByNameOrId(itemNameOrId);
        final var objectId = new MongoInventoryItemId(mongoUser, mongoItem, priority);

        final var query = getDatastore().find(MongoInventoryItem.class);

        query.filter(eq("_id", objectId));

        final MongoInventoryItem mongoInventoryItem = query.first();

        final var builder = new UpdateBuilder()
            .with(set("version", randomUUID().toString()));

        if (mongoInventoryItem == null) {
            builder.with(set("user", mongoUser))
                   .with(set("item", mongoItem));
        }

        final var opts = new ModifyOptions()
            .upsert(true)
            .writeConcern(WriteConcern.ACKNOWLEDGED)
            .returnDocument(ReturnDocument.AFTER);

        final var resultMongoInventoryItem = builder.with(
                set("quantity", quantity),
                set("version", randomUUID().toString()))
            .execute(query, opts);

        return getDozerMapper().map(resultMongoInventoryItem, InventoryItem.class);

    }

    @Override
    public InventoryItem adjustQuantityForItem(final String inventoryItemId, final int quantityDelta) {
        final var objectId = MongoInventoryItemId.parseOrThrowNotFoundException(inventoryItemId);
        return adjustQuantityForItem(objectId, quantityDelta);
    }

    @Override
    public InventoryItem adjustQuantityForItem(final User user,
                                               final String itemNameOrId,
                                               final int priority, final int quantityDelta) {
        final var mongoUser = getMongoUserDao().getMongoUser(user.getId());
        final var mongoItem = getMongoItemDao().getMongoItemByNameOrId(itemNameOrId);
        final var objectId = new MongoInventoryItemId(mongoUser, mongoItem, priority);
        return adjustQuantityForItem(objectId, quantityDelta);
    }

    public InventoryItem adjustQuantityForItem(final MongoInventoryItemId objectId, final int quantityDelta) {

        final MongoInventoryItem mongoInventoryItem;

        try {
            mongoInventoryItem = getMongoConcurrentUtils().performOptimistic(
                    ads -> doAdjustQuantityForItem(objectId, quantityDelta)
            );
        } catch (MongoConcurrentUtils.ConflictException ex) {
            throw new TooBusyException(ex);
        }

        return getDozerMapper().map(mongoInventoryItem, InventoryItem.class);

    }

    private MongoInventoryItem doAdjustQuantityForItem(final MongoInventoryItemId objectId,
                                                       final int quantityDelta) throws ContentionException {

        final Query<MongoInventoryItem> query = getDatastore().find(MongoInventoryItem.class);
        query.filter(eq("_id", objectId));

        final MongoInventoryItem mongoInventoryItem = query.first();

        query.filter(eq("version",
            mongoInventoryItem == null
                ? randomUUID().toString()
                : mongoInventoryItem.getVersion()
            )
        );

        final var builder = new UpdateBuilder();

        final int base;

        if (mongoInventoryItem == null) {

            final MongoItem mongoItem = getMongoItemDao().getMongoItem(objectId.getItemObjectId());
            final MongoUser mongoUser = getMongoUserDao().getMongoUser(objectId.getUserObjectId());

            base = 0;

            builder.with(
                set("user", mongoUser),
                set("item", mongoItem)
            );

        } else {
            base = mongoInventoryItem.getQuantity();
        }

        final int quantity = max(0, base + quantityDelta);

        final var opts = new ModifyOptions()
            .upsert(true)
            .writeConcern(WriteConcern.ACKNOWLEDGED)
            .returnDocument(ReturnDocument.AFTER);

        final var item = builder.with(
            set("quantity", quantity),
            set("version", randomUUID().toString())
        ).execute(query, opts);

        if (item == null) {
            throw new ContentionException();
        }

        return item;

    }

    @Override
    public void deleteInventoryItem(final String inventoryItemId) {

        final MongoInventoryItemId objectId = parseOrThrowNotFoundException(inventoryItemId);
        final DeleteResult deleteResult = getDatastore().find(MongoInventoryItem.class)
                .filter(eq("_id", objectId)).delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Item Inventory not found: " + inventoryItemId);
        }

    }

    private void normalize(InventoryItem item) {
        // leave this stub here in case we implement some normalization logic later
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MongoItemDao getMongoItemDao() {
        return mongoItemDao;
    }

    @Inject
    public void setMongoItemDao(MongoItemDao mongoItemDao) {
        this.mongoItemDao = mongoItemDao;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoConcurrentUtils getMongoConcurrentUtils() {
        return mongoConcurrentUtils;
    }

    @Inject
    public void setMongoConcurrentUtils(MongoConcurrentUtils mongoConcurrentUtils) {
        this.mongoConcurrentUtils = mongoConcurrentUtils;
    }


}
