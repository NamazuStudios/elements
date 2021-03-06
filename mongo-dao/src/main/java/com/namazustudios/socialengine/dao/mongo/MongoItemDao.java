package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.model.ReturnDocument;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.item.ItemNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MongoItemDao implements ItemDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoItemDao.class);

    private StandardQueryParser standardQueryParser;

    private ObjectIndex objectIndex;

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public Item getItemByIdOrName(final String identifier) {

        final MongoItem item = getMongoItemByNameOrId(identifier);

        if (item == null) {
            throw new NotFoundException("Unable to find item with an id or name of " + identifier);
        }

        return getDozerMapper().map(item, Item.class);
    }

    public MongoItem getMongoItem(final Item item) {
        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(item == null ? null : item.getId());
        return getMongoItem(objectId);
    }

    public MongoItem getMongoItem(final ObjectId objectId) {

        final MongoItem mongoItem = getDatastore().find(MongoItem.class)
                .filter(eq("_id", objectId)).first();

        if(null == mongoItem) {
            throw new NotFoundException("Unable to find item with an id of " + objectId);
        }

        return mongoItem;

    }

    public MongoItem getMongoItemByNameOrId(final String itemNameOrId) {

        if (isEmpty(nullToEmpty(itemNameOrId).trim())) {
            throw new NotFoundException("Unable to find item with an id of " + itemNameOrId);
        }

        final Query<MongoItem> itemQuery = getDatastore().find(MongoItem.class);

        if (ObjectId.isValid(itemNameOrId)) {
            itemQuery.filter(eq("_id", new ObjectId(itemNameOrId)));
        } else {
            itemQuery.filter(eq("name", itemNameOrId));
        }

        final MongoItem mongoItem = itemQuery.first();

        if(null == mongoItem) {
            throw new NotFoundException("Unable to find item with an id of " + itemNameOrId);
        }

        return mongoItem;

    }

    public MongoItem refresh(final MongoItem mongoItem) {

        final Query<MongoItem> query = getDatastore().find(MongoItem.class);

        final String identifier;

        if (mongoItem.getObjectId() != null) {
            query.filter(eq("_id", mongoItem.getObjectId()));
            identifier = mongoItem.getObjectId().toHexString();
        } else if (mongoItem.getName() != null) {
            query.filter(eq("name", mongoItem.getName()));
            identifier = mongoItem.getName();
        } else {
            throw new InvalidDataException("Must specify Item name or id");
        }

        final MongoItem refreshedMongoItem = query.first();

        if (refreshedMongoItem == null) {
            throw new ItemNotFoundException("Unable to find item with an id or name of " + identifier);
        }

        return refreshedMongoItem;

    }

    @Override
    public Pagination<Item> getItems(final int offset, final int count, List<String> tags, final String query) {
        if (StringUtils.isNotEmpty(query)) {
            LOGGER.warn(" getItems(int offset, int count, List<String> tags, String query) was called with a query " +
                        "string parameter.  This field is presently ignored and will return all values after filtering " +
                        "by tags");
        }

        final Query<MongoItem> mongoQuery = getDatastore().find(MongoItem.class);

        if (tags != null && !tags.isEmpty()) {
            mongoQuery.filter(Filters.in("tags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count,
            mongoItem -> getDozerMapper().map(mongoItem, Item.class), new FindOptions());
    }

    @Override
    public Item updateItem(final Item item) {

        validate(item);
        normalize(item);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(item.getId());
        final var query = getDatastore().find(MongoItem.class);
        query.filter(eq("_id", objectId));

        final var updatedMongoItem = getMongoDBUtils().perform(ds ->
            query.modify(
                set("name", item.getName()),
                set("displayName", item.getDisplayName()),
                set("metadata", item.getMetadata()),
                set("tags", item.getTags()),
                set("description", item.getDescription())
            ).execute(new ModifyOptions().upsert(false).returnDocument(ReturnDocument.AFTER))
        );

        if (updatedMongoItem == null) {
            throw new ItemNotFoundException("Item with ID not found: " + item.getId());
        }

        getObjectIndex().index(updatedMongoItem);
        return getDozerMapper().map(updatedMongoItem, Item.class);

    }

    @Override
    public Item createItem(Item item) {
        validate(item);
        normalize(item);

        final MongoItem mongoItem = getDozerMapper().map(item, MongoItem.class);

        try {
            getDatastore().save(mongoItem);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }
        getObjectIndex().index(mongoItem);

        final Query<MongoItem> query = getDatastore().find(MongoItem.class);
        query.filter(eq("_id", mongoItem.getObjectId()));

        return getDozerMapper().map(query.first(), Item.class);
    }

    private void validate(Item item) {
        if (item == null) {
            throw new InvalidDataException("Item must not be null.");
        }
        getValidationHelper().validateModel(item);
    }

    private void normalize(Item item) {
        item.setDisplayName(item.getDisplayName().trim());
        item.setDescription(item.getDescription().trim());
        item.validateTags();
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
}
