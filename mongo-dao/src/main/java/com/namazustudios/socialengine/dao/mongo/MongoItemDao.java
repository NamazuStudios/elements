package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoItem;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.Item;
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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class MongoItemDao implements ItemDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoItemDao.class);

    private StandardQueryParser standardQueryParser;

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public Item getItemByIdOrName(String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            throw new NotFoundException("Unable to find item with an id or name of " + identifier);
        }

        Query<MongoItem> query = getDatastore().createQuery(MongoItem.class);

        if (ObjectId.isValid(identifier)) {
            query.criteria("_id").equal(new ObjectId(identifier));
        } else {
            query.criteria("name").equal(identifier);
        }

        final MongoItem item = query.get();

        if (item == null) {
            throw new NotFoundException("Unable to find item with an id or name of " + identifier);
        }

        return getDozerMapper().map(item, Item.class);
    }

    @Override
    public Pagination<Item> getItems(int offset, int count, Set<String> tags, String query) {
        if (StringUtils.isNotEmpty(query)) {
            LOGGER.warn(" getItems(int offset, int count, Set<String> tags, String query) was called with a query " +
                        "string parameter.  This field is presently ignored and will return all values after filtering " +
                        "by tags");
        }

        final Query<MongoItem> mongoQuery = getDatastore().createQuery(MongoItem.class);

        if (tags != null && !tags.isEmpty()) {
            mongoQuery.criteria("tags").hasAnyOf(tags);
        }
        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count,
            mongoItem -> getDozerMapper().map(mongoItem, Item.class));
    }

    @Override
    public Item updateItem(Item item) {
        validate(item);

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(item.getId());

        final Query<MongoItem> query = getDatastore().createQuery(MongoItem.class);
        query.criteria("_id").equal(objectId);

        final UpdateOperations<MongoItem> operations = getDatastore().createUpdateOperations(MongoItem.class);
        operations.set("name", item.getName());
        operations.set("displayName", item.getDisplayName());
        operations.set("metadata", item.getMetadata());
        operations.set("tags", item.getTags());
        operations.set("description", item.getDescription());

        final FindAndModifyOptions options = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        final MongoItem updatedMongoItem = getDatastore().findAndModify(query, operations, options);
        if (updatedMongoItem == null) {
            throw new NotFoundException("Item with id or name of " + item.getId() + " does not exist");
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

        return getDozerMapper().map(getDatastore().get(mongoItem), Item.class);
    }

    private void validate(Item item) {
        if (item == null) {
            throw new InvalidDataException("Item must not be null.");
        }

    }

    private void normalize(Item item) {
        item.setDisplayName(item.getDisplayName().trim());
        item.setDescription(item.getDescription().trim());
        item.setTags(new HashSet<>(item.getTags().stream().filter(Objects::nonNull).map(this::normalizeTag).collect(Collectors.toSet())));
    }

    private String normalizeTag(String input) {
        return input == null ? null : input.trim().toLowerCase().replaceAll("\\W", "_");
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
