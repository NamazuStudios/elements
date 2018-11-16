package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoInventoryItem;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.Pagination;
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

@Singleton
public class MongoInventoryItemDao implements InventoryItemDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoInventoryItemDao.class);

    private StandardQueryParser standardQueryParser;

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public InventoryItem getInventoryItem(String inventoryItemId) {
        if (StringUtils.isEmpty(inventoryItemId)) {
            throw new NotFoundException("Unable to find item with an id of " + inventoryItemId);
        }

        Query<MongoInventoryItem> query = getDatastore().createQuery(MongoInventoryItem.class);

        if (ObjectId.isValid(inventoryItemId)) {
            query.criteria("_id").equal(new ObjectId(inventoryItemId));
        } else {
            throw new NotFoundException("Unable to find item with an id of " + inventoryItemId);
        }

        final MongoInventoryItem item = query.get();

        if (item == null) {
            throw new NotFoundException("Unable to find item with an id of " + inventoryItemId);
        }

        return getDozerMapper().map(item, InventoryItem.class);
    }

    @Override
    public InventoryItem getInventoryItemByItemNameOrId(String itemNameOrId) {
        if (StringUtils.isEmpty(itemNameOrId)) {
            throw new NotFoundException("Unable to find item with an id of " + itemNameOrId);
        }

        Query<MongoInventoryItem> query = getDatastore().createQuery(MongoInventoryItem.class);

        if (ObjectId.isValid(itemNameOrId)) {
            query.criteria("item._id").equal(new ObjectId(itemNameOrId));
        } else {
            query.criteria("item.name").equal(new ObjectId(itemNameOrId));
        }

        query.order("priority");

        final MongoInventoryItem item = query.get();

        if (item == null) {
            throw new NotFoundException("Unable to find item with an id of " + itemNameOrId);
        }

        return getDozerMapper().map(item, InventoryItem.class);
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(int offset, int count) { return getInventoryItems(offset, count, null); }

    @Override
    public Pagination<InventoryItem> getInventoryItems(int offset, int count, String query) {
        if (StringUtils.isNotEmpty(query)) {
            LOGGER.warn(" getItems(int offset, int count, String query) was called with a query " +
                    "string parameter.  This field is presently ignored and will return all values");
        }

        final Query<MongoInventoryItem> mongoQuery = getDatastore().createQuery(MongoInventoryItem.class);

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count,
                mongoItem -> getDozerMapper().map(mongoItem, InventoryItem.class));
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(String itemNameOrId, int offset, int count) {
        final Query<MongoInventoryItem> mongoQuery = getDatastore().createQuery(MongoInventoryItem.class);

        if (ObjectId.isValid(itemNameOrId)) {
            mongoQuery.criteria("item._id").equal(new ObjectId(itemNameOrId));
        } else {
            mongoQuery.criteria("item.name").equal(itemNameOrId);
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count,
                mongoItem -> getDozerMapper().map(mongoItem, InventoryItem.class));
    }

    @Override
    public InventoryItem updateInventoryItem(InventoryItem inventoryItem) {
        validate(inventoryItem);

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(inventoryItem.getId());

        final Query<MongoInventoryItem> query = getDatastore().createQuery(MongoInventoryItem.class);
        query.criteria("_id").equal(objectId);

        final UpdateOperations<MongoInventoryItem> operations = getDatastore().createUpdateOperations(MongoInventoryItem.class);
        operations.set("user", inventoryItem.getUser());
        operations.set("item", inventoryItem.getItem());
        operations.set("quantity", inventoryItem.getQuantity());

        final FindAndModifyOptions options = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        final MongoInventoryItem mongoInventoryItem = getDatastore().findAndModify(query, operations, options);
        if (mongoInventoryItem == null) {
            throw new NotFoundException("Inventory item with id of " + inventoryItem.getId() + " does not exist");
        }
        getObjectIndex().index(mongoInventoryItem);

        return getDozerMapper().map(mongoInventoryItem, InventoryItem.class);
    }

    @Override
    public InventoryItem createInventoryItem(InventoryItem item) {
        validate(item);
        normalize(item);

        final MongoInventoryItem mongoItem = getDozerMapper().map(item, MongoInventoryItem.class);

        try {
            getDatastore().save(mongoItem);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }
        getObjectIndex().index(mongoItem);

        return getDozerMapper().map(getDatastore().get(mongoItem), InventoryItem.class);
    }

    @Override
    public void deleteInventoryItem(String inventoryItemId) {
        final ObjectId registrationId = getMongoDBUtils().parseOrThrowNotFoundException(inventoryItemId);
        final WriteResult writeResult = getDatastore().delete(MongoItemDao.class, registrationId);

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
}
