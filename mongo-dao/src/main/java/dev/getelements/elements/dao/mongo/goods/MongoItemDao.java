package dev.getelements.elements.dao.mongo.goods;

import com.mongodb.client.model.ReturnDocument;
import dev.getelements.elements.sdk.dao.ItemDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpec;
import dev.getelements.elements.dao.mongo.schema.MongoMetadataSpecDao;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.item.ItemNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import org.apache.commons.lang3.StringUtils;

import org.bson.types.ObjectId;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MongoItemDao implements ItemDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoItemDao.class);

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MapperRegistry dozerMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    private MongoMetadataSpecDao mongoMetadataSpecDao;

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

    public List<MongoItem> getPublicItems() {
        final Query<MongoItem> mongoQuery = getDatastore().find(MongoItem.class);
        mongoQuery.filter(eq("publicVisible", true));

        return mongoQuery.iterator().toList();
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

        final Query<MongoItem> mongoQuery = getDatastore().find(MongoItem.class).filter(exists("name"));

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

        if (StringUtils.isNotEmpty(query)) {
            mongoQuery.filter(
                    Filters.regex("name", Pattern.compile(query))
            );
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

        if (!isNull(item.getMetadataSpec())) {
            return updateItemWtihMetadata(item ,query);
        }

        final var options = new ModifyOptions().upsert(false).returnDocument(ReturnDocument.AFTER);

        final var updatedMongoItem = getMongoDBUtils().perform(ds ->
                query.modify(options,
                        set("name", item.getName()),
                        set("displayName", item.getDisplayName()),
                        set("metadata", item.getMetadata()),
                        set("tags", item.getTags()),
                        set("description", item.getDescription()),
                        set("publicVisible", item.isPublicVisible()),
                        set("category", item.getCategory())
                )
        );

        if (updatedMongoItem == null) {
            throw new ItemNotFoundException("Item with ID not found: " + item.getId());
        }

        return getDozerMapper().map(updatedMongoItem, Item.class);

    }

    public Item updateItemWtihMetadata(final Item item, Query<MongoItem> query) {
        final var mongoMetadataSpec = isNull(item.getMetadataSpec()) ? null :
                getMongoMetadataSpec(item.getMetadataSpec().getId());

        final var options = new ModifyOptions().upsert(false).returnDocument(ReturnDocument.AFTER);

        final var updatedMongoItem = getMongoDBUtils().perform(ds ->
                query.modify(options,
                        set("name", item.getName()),
                        set("displayName", item.getDisplayName()),
                        set("metadataSpec", Objects.requireNonNull(mongoMetadataSpec)),
                        set("metadata", item.getMetadata()),
                        set("tags", item.getTags()),
                        set("description", item.getDescription()),
                        set("publicVisible", item.isPublicVisible()),
                        set("category", item.getCategory())
                )
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

        final var mongoMetadataSpec = isNull(item.getMetadataSpec()) ? null :
        getMongoMetadataSpec(item.getMetadataSpec().getId());
        mongoItem.setMetadataSpec(mongoMetadataSpec);

        getMongoDBUtils().perform(ds -> getDatastore().save(mongoItem));

        final Query<MongoItem> query = getDatastore().find(MongoItem.class);
        query.filter(eq("_id", mongoItem.getObjectId()));

        return getDozerMapper().map(query.first(), Item.class);
    }

    @Override
    public void deleteItem(String identifier) {

        final var objectId = getMongoDBUtils().parseOrReturnNull(identifier);
        final var filter = objectId != null ? eq("_id", objectId) : eq("name", identifier);
        final var options = new ModifyOptions().returnDocument(ReturnDocument.AFTER);

        getDatastore().find(MongoItem.class)
                .filter(filter)
                .modify(options, unset("name"));
    }

    private void normalize(Item item) {
        item.setDisplayName(item.getDisplayName().trim());
        item.setDescription(item.getDescription().trim());
        item.validateTags();
    }

    private MongoMetadataSpec getMongoMetadataSpec(final String specId) {
        return isNullOrEmpty(specId) ? null : getMongoMetadataSpecDao().findActiveMongoMetadataSpec(specId)
            .orElseThrow(()-> new NotFoundException(format("Not found metadataspec object %s", specId)));
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

    public MongoMetadataSpecDao getMongoMetadataSpecDao() {
        return mongoMetadataSpecDao;
    }

    @Inject
    public void setMongoMetadataSpecDao(MongoMetadataSpecDao mongoMetadataSpecDao) {
        this.mongoMetadataSpecDao = mongoMetadataSpecDao;
    }
}
