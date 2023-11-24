package dev.getelements.elements.dao.mongo.goods;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.model.ReturnDocument;
import dev.getelements.elements.dao.ItemDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.exception.item.ItemNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.goods.ItemCategory;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.model.goods.ItemCategory.DISTINCT;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.or;
import static dev.morphia.query.updates.UpdateOperators.set;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MongoItemDao implements ItemDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoItemDao.class);

    private StandardQueryParser standardQueryParser;

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public Item getItemByIdOrName(final String identifier) {
        return findMongoItemByNameOrId(identifier)
            .map(mi -> getDozerMapper().map(mi, Item.class))
            .orElseThrow(() -> new ItemNotFoundException("Unable to find item with an id or name of " + identifier));
    }

    public Optional<MongoItem> findMongoItem(final Item item) {
        return Optional.ofNullable(item).flatMap(i -> findMongoItem(i.getId()));
    }

    public Optional<MongoItem> findMongoItem(final String id) {
        return Optional.ofNullable(id)
            .flatMap(i -> getMongoDBUtils().parse(i))
            .flatMap(this::findMongoItem);
    }

    public Optional<MongoItem> findMongoItem(final ObjectId objectId) {
        final var item = getDatastore()
            .find(MongoItem.class)
            .filter(eq("_id", objectId)).first();
        return Optional.ofNullable(item);
    }

    public MongoItem getMongoItem(final Item item) {
        final var id = item == null ? null : item.getId();
        final var objectId = getMongoDBUtils().parseOrThrow(id, ItemNotFoundException::new);
        return getMongoItem(objectId);
    }

    public MongoItem getMongoItem(final ObjectId objectId) {
        return findMongoItem(objectId)
            .orElseThrow(() -> new NotFoundException("Unable to find item with an id of " + objectId));
    }

    public Optional<MongoItem> findMongoItemByNameOrId(final String itemNameOrId) {

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
        return Optional.ofNullable(mongoItem);

    }

    public MongoItem getMongoItemByNameOrId(final String itemNameOrId) {
        return findMongoItemByNameOrId(itemNameOrId).orElseThrow(() -> new NotFoundException("Unable to find item with an id of " + itemNameOrId));
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
    public Pagination<Item> getItems(final int offset, final int count, List<String> tags, String category, final String query) {

        if (StringUtils.isNotEmpty(query)) {
            LOGGER.warn(" getItems(int offset, int count, List<String> tags, String query) was called with a query " +
                        "string parameter.  This field is presently ignored and will return all values after filtering " +
                        "by tags");
        }

        final Query<MongoItem> mongoQuery = getDatastore().find(MongoItem.class);

        if (tags != null && !tags.isEmpty()) {
            mongoQuery.filter(Filters.in("tags", tags));
        }

        if (category != null && !category.isBlank()) {

            final ItemCategory categoryEnum;

            try {
                categoryEnum = ItemCategory.valueOf(category);
            } catch (IllegalArgumentException ex) {
                return Pagination.empty();
            }

            mongoQuery.filter(eq("category", categoryEnum));

        }

        return getMongoDBUtils().paginationFromQuery(
            mongoQuery,
            offset, count,
            mongoItem -> getDozerMapper().map(mongoItem, Item.class),
            new FindOptions()
        );

    }

    @Override
    public Item updateItem(final Item item) {

        getValidationHelper().validateModel(item, ValidationGroups.Update.class);
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
                set("description", item.getDescription()),
                set("publicVisible", item.getPublicVisible()),
                set("category", item.getCategory())
            ).execute(new ModifyOptions().upsert(false).returnDocument(ReturnDocument.AFTER))
        );

        if (updatedMongoItem == null) {
            throw new ItemNotFoundException("Item with ID not found: " + item.getId());
        }

        return getDozerMapper().map(updatedMongoItem, Item.class);

    }

    @Override
    public Item createItem(Item item) {

        getValidationHelper().validateModel(item, ValidationGroups.Insert.class);
        normalize(item);

        final MongoItem mongoItem = getDozerMapper().map(item, MongoItem.class);

        try {
            getDatastore().save(mongoItem);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        final Query<MongoItem> query = getDatastore().find(MongoItem.class);
        query.filter(eq("_id", mongoItem.getObjectId()));

        return getDozerMapper().map(query.first(), Item.class);
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

}
