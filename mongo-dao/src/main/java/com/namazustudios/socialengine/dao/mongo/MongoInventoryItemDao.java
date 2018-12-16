package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItem;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId.parseOrThrowNotFoundException;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Singleton
public class MongoInventoryItemDao implements InventoryItemDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoInventoryItemDao.class);

    private StandardQueryParser standardQueryParser;

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private MongoItemDao mongoItemDao;

    @Override
    public InventoryItem getInventoryItem(final String inventoryItemId) {

        if (isEmpty(inventoryItemId)) {
            throw new NotFoundException("Unable to find item with an id of " + inventoryItemId);
        }

        final MongoInventoryItemId objectId = parseOrThrowNotFoundException(inventoryItemId);
        final Query<MongoInventoryItem> query = getDatastore().createQuery(MongoInventoryItem.class);

        query.criteria("_id").equal(objectId);

        final MongoInventoryItem item = query.get();

        if (item == null) {
            throw new NotFoundException("Unable to find item with an id of " + inventoryItemId);
        }

        return getDozerMapper().map(item, InventoryItem.class);
    }

    @Override
    public InventoryItem getInventoryItemByItemNameOrId(final User user, final String itemNameOrId) {

        final Query<MongoInventoryItem> query = getDatastore().createQuery(MongoInventoryItem.class);

        query.criteria("user").equal(getDozerMapper().map(user, MongoUser.class));
        query.criteria("item").equal(getMongoItemDao().getMongoItemByNameOrId(itemNameOrId));

        final MongoInventoryItem item = query.get();

        if (item == null) {
            throw new NotFoundException("Unable to find item with an id of " + itemNameOrId);
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

        final Query<MongoInventoryItem> query = getDatastore().createQuery(MongoInventoryItem.class);

        query.criteria("user").equal(getDozerMapper().map(user, MongoUser.class));

        return getMongoDBUtils().paginationFromQuery(query, offset, count,
                mongoItem -> getDozerMapper().map(mongoItem, InventoryItem.class));
    }

    @Override
    public InventoryItem getInventoryItem(final User user, final String itemNameOrId,
                                          final int offset, final int count) {

        final Query<MongoInventoryItem> query = getDatastore().createQuery(MongoInventoryItem.class);

        query.criteria("user").equal(getDozerMapper().map(user, MongoUser.class));
        query.criteria("item").equal(getMongoItemDao().getMongoItemByNameOrId(itemNameOrId));

        final MongoInventoryItem mongoSimpleInventoryItem = query.get();

        if (mongoSimpleInventoryItem == null) {
            throw new NotFoundException("Item no inventory item found for item and user.");
        }

        return getDozerMapper().map(mongoSimpleInventoryItem, InventoryItem.class);

    }

    @Override
    public InventoryItem updateInventoryItem(final InventoryItem simpleInventoryItem) {

        validate(simpleInventoryItem);

        final MongoInventoryItemId objectId = MongoInventoryItemId.parseOrThrowNotFoundException(simpleInventoryItem.getId());

        final Query<MongoInventoryItem> query = getDatastore().createQuery(MongoInventoryItem.class);
        query.criteria("_id").equal(objectId);

        final MongoItem mongoItem = getMongoItemDao().getMongoItem(simpleInventoryItem.getItem());
        final UpdateOperations<MongoInventoryItem> operations = getDatastore().createUpdateOperations(MongoInventoryItem.class);

        operations.set("_id", mongoItem.getObjectId());
        operations.set("user", getDozerMapper().map(simpleInventoryItem.getUser(), MongoUser.class));
        operations.set("item", getDozerMapper().map(simpleInventoryItem.getItem(), MongoItem.class));
        operations.set("quantity", simpleInventoryItem.getQuantity());

        final FindAndModifyOptions options = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        final MongoInventoryItem mongoInventoryItem = getDatastore().findAndModify(query, operations, options);

        if (mongoInventoryItem == null) {
            throw new NotFoundException("Inventory item with id of " + simpleInventoryItem.getId() + " does not exist");
        }

        getObjectIndex().index(mongoInventoryItem);

        return getDozerMapper().map(mongoInventoryItem, InventoryItem.class);
    }

    @Override
    public InventoryItem createInventoryItem(final InventoryItem simpleInventoryItem) {

        validate(simpleInventoryItem);
        normalize(simpleInventoryItem);

        final MongoItem mongoItem = getMongoItemDao().getMongoItem(simpleInventoryItem.getItem());
        final MongoInventoryItem mongoInventoryItem = getDozerMapper().map(simpleInventoryItem, MongoInventoryItem.class);

        try {
            getDatastore().save(mongoInventoryItem);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException(ex);
        }

        getObjectIndex().index(mongoInventoryItem);
        return getDozerMapper().map(getDatastore().get(mongoInventoryItem), InventoryItem.class);

    }

    @Override
    public void deleteInventoryItem(final String inventoryItemId) {

        final ObjectId id = getMongoDBUtils().parseOrThrowNotFoundException(inventoryItemId);
        final WriteResult writeResult = getDatastore().delete(MongoInventoryItem.class, id);

        if (writeResult.getN() == 0) {
            throw new NotFoundException("Item Inventory not found: " + inventoryItemId);
        }

    }

    private void validate(InventoryItem item) {
        if (item == null) {
            throw new InvalidDataException("Inventory item must not be null.");
        }
        getValidationHelper().validateModel(item);
    }

    private void normalize(InventoryItem item) {
        // leave this stub here in case we implement some normalization logic later
    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
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

}
