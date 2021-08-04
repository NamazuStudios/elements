package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.ContentionException;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItem;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.TooBusyException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.dozer.Mapper;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId.parseOrThrowNotFoundException;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;
import static java.lang.Integer.max;
import static java.util.UUID.randomUUID;

public class MongoInventoryItemDao implements InventoryItemDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoInventoryItemDao.class);

    private StandardQueryParser standardQueryParser;

    private ObjectIndex objectIndex;

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

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

        final var mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final var mongoItem = getMongoItemDao().getMongoItemByNameOrId(itemNameOrId);
        final var objectId = new MongoInventoryItemId(mongoUser, mongoItem, priority);

        final MongoInventoryItem item = getDatastore()
            .find(MongoInventoryItem.class)
            .filter(Filters.eq("_id", objectId))
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
    public Pagination<InventoryItem> getInventoryItems(final User user, final int offset, final int count) {
        return getInventoryItems(user, offset, count, null);
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final User user,
                                                       final int offset, final int count,
                                                       final String search) {
        if (StringUtils.isNotEmpty(search)) {
            LOGGER.warn(" getItems(int offset, int count, String query) was called with a query " +
                    "string parameter.  This field is presently ignored and will return all values");
        }

        final Query<MongoInventoryItem> query = getDatastore().find(MongoInventoryItem.class);

        query.filter(Filters.eq("user", getDozerMapper().map(user, MongoUser.class)));

        return getMongoDBUtils().paginationFromQuery(
            query, offset, count,
            mongoItem -> getDozerMapper().map(mongoItem, InventoryItem.class), new FindOptions());
    }

    @Override
    public InventoryItem createInventoryItem(final InventoryItem inventoryItem) {

        getValidationHelper().validateModel(inventoryItem, Insert.class);

        normalize(inventoryItem);

        final var mongoItem = getMongoItemDao().getMongoItem(inventoryItem.getItem());
        final var mongoUser = getMongoUserDao().getActiveMongoUser(inventoryItem.getUser());

        final MongoInventoryItem mongoInventoryItem = getDozerMapper().map(inventoryItem, MongoInventoryItem.class);

        mongoInventoryItem.setVersion(randomUUID().toString());
        mongoInventoryItem.setObjectId(new MongoInventoryItemId(mongoUser, mongoItem, inventoryItem.getPriority()));

        try {
            getDatastore().insert(mongoInventoryItem);
        } catch (MongoException ex) {
            if (ex.getCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        getObjectIndex().index(mongoInventoryItem);

        final Query<MongoInventoryItem> query = getDatastore().find(MongoInventoryItem.class);
        query.filter(Filters.eq("_id", mongoInventoryItem.getObjectId()));

        return getDozerMapper().map(query.first(), InventoryItem.class);

    }

    @Override
    public InventoryItem updateInventoryItem(final InventoryItem inventoryItem) {

        getValidationHelper().validateModel(inventoryItem, Update.class);

        final var query = getDatastore().find(MongoInventoryItem.class);

        final var objectId = parseOrThrowNotFoundException(inventoryItem.getId());
        query.filter(Filters.eq("_id", objectId));

        final var mongoInventoryItem = getMongoDBUtils().perform(ds ->
            query.modify(
                set("version", randomUUID().toString()),
                set("quantity", inventoryItem.getQuantity())
            ).execute(new ModifyOptions().upsert(true).returnDocument(ReturnDocument.AFTER))
        );

        if (mongoInventoryItem == null) {
            throw new NotFoundException("Inventory item with id of " + inventoryItem.getId() + " does not exist");
        }

        getObjectIndex().index(mongoInventoryItem);
        return getDozerMapper().map(mongoInventoryItem, InventoryItem.class);

    }

    @Override
    public InventoryItem setQuantityForItem(
            final User user,
            final String itemNameOrId,
            final int priority,
            final int quantity) {

        if (quantity < 0) throw new IllegalArgumentException("invalid quantity: " + quantity);

        final var mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final var mongoItem = getMongoItemDao().getMongoItemByNameOrId(itemNameOrId);
        final var objectId = new MongoInventoryItemId(mongoUser, mongoItem, priority);

        final var query = getDatastore().find(MongoInventoryItem.class);

        query.filter(Filters.eq("_id", objectId));

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
    public InventoryItem adjustQuantityForItem(
            final User user,
            final String itemNameOrId,
            final int priority,
            final int quantityDelta) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final MongoItem mongoItem = getMongoItemDao().getMongoItemByNameOrId(itemNameOrId);
        final MongoInventoryItemId objectId = new MongoInventoryItemId(mongoUser, mongoItem, priority);

        final MongoInventoryItem mongoInventoryItem;

        try {
            mongoInventoryItem = getMongoConcurrentUtils().performOptimistic(
                ads -> doAdjustQuantityForItem(objectId, quantityDelta)
            );
        } catch (MongoConcurrentUtils.ConflictException ex) {
            throw new TooBusyException(ex);
        }

        getObjectIndex().index(mongoInventoryItem);
        return getDozerMapper().map(mongoInventoryItem, InventoryItem.class);

    }

    private MongoInventoryItem doAdjustQuantityForItem(final MongoInventoryItemId objectId, final int quantityDelta) throws ContentionException {

        final Query<MongoInventoryItem> query = getDatastore().find(MongoInventoryItem.class);
        query.filter(Filters.eq("_id", objectId));

        final MongoInventoryItem mongoInventoryItem = query.first();
        query.filter(Filters.eq("version", mongoInventoryItem == null ? randomUUID().toString() : mongoInventoryItem.getVersion()));

        final var builder = new UpdateBuilder();

        final int base;

        if (mongoInventoryItem == null) {

            final MongoItem mongoItem = getMongoItemDao().getMongoItem(objectId.getItemObjectId());
            final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(objectId.getUserObjectId());

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
                .filter(Filters.eq("_id", objectId)).delete();

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

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
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


    public StandardQueryParser getStandardQueryParser() {
        return standardQueryParser;
    }

    @Inject
    public void setStandardQueryParser(StandardQueryParser standardQueryParser) {
        this.standardQueryParser = standardQueryParser;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
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
