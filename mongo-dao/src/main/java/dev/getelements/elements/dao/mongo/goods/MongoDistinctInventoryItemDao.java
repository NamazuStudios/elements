package dev.getelements.elements.dao.mongo.goods;

import com.mongodb.client.model.ReturnDocument;
import dev.getelements.elements.sdk.dao.DistinctInventoryItemDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoProfileDao;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.goods.MongoDistinctInventoryItem;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.inventory.DistinctInventoryItemNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static dev.getelements.elements.sdk.model.goods.ItemCategory.DISTINCT;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoDistinctInventoryItemDao implements DistinctInventoryItemDao {

    private MapperRegistry mapperRegistry;

    private Datastore datastore;

    private MongoItemDao mongoItemDao;

    private MongoUserDao mongoUserDao;

    private MongoProfileDao mongoProfileDao;

    private ValidationHelper validationHelper;

    private MongoDBUtils mongoDBUtils;

    private BooleanQueryParser booleanQueryParser;

    @Override
    public DistinctInventoryItem createDistinctInventoryItem(final DistinctInventoryItem distinctInventoryItem) {

        getValidationHelper().validateModel(distinctInventoryItem, Insert.class);

        final var mongoDistinctInventoryItem = new MongoDistinctInventoryItem();

        final var mongoItem = getMongoItemDao()
                .findMongoItem(distinctInventoryItem.getItem())
                .orElseThrow(() -> new InvalidDataException("No such item."));

        if (!DISTINCT.equals(mongoItem.getCategory())) {
            var category = ItemCategory.getOrDefault(mongoItem.getCategory());
            throw new InvalidDataException("Invalid item category: " + category);
        }

        final var mongoUser = getMongoUserDao()
                .findMongoUser(distinctInventoryItem.getUser().getId())
                .orElseThrow(() -> new InvalidDataException("No such user."));

        mongoDistinctInventoryItem.setItem(mongoItem);
        mongoDistinctInventoryItem.setUser(mongoUser);

        Optional.ofNullable(distinctInventoryItem.getProfile())
                .flatMap(p -> getMongoProfileDao().findActiveMongoProfile(p))
                .ifPresent(mongoDistinctInventoryItem::setProfile);

        mongoDistinctInventoryItem.setMetadata(distinctInventoryItem.getMetadata());

        final var result = getDatastore().save(mongoDistinctInventoryItem);
        return getMapper().map(result, DistinctInventoryItem.class);

    }

    @Override
    public DistinctInventoryItem getDistinctInventoryItem(final String id) {

        final var objectId = getMongoDBUtils().parseOrThrow(id, DistinctInventoryItemNotFoundException::new);

        final var result = getDatastore()
                .find(MongoDistinctInventoryItem.class)
                .filter(eq("_id", objectId))
                .first();

        if (result == null) {
            throw new DistinctInventoryItemNotFoundException("No such inventory item.");
        }

        return getMapper().map(result, DistinctInventoryItem.class);

    }

    @Override
    public Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            final int offset, final int count,
            final String userId, final String profileId, final boolean publicOnly) {

        return getDistinctInventoryItems(offset, count, userId, profileId, publicOnly,null);


    }

    @Override
    public Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            final int offset, final int count,
            final String userId, final String profileId,
            final boolean publicOnly, final String queryString) {

        final var query = getDatastore().find(MongoDistinctInventoryItem.class);

        if (userId != null && !userId.isBlank()) {
            var user = getMongoUserDao().findMongoUser(userId);
            if (user.isEmpty()) {
                return Pagination.empty();
            }
            user.ifPresent(u -> query.filter(eq("user", u)));

        }

        if (profileId != null && !profileId.isBlank()) {
            var profile = getMongoProfileDao().findActiveMongoProfile(profileId);
            if (profile.isEmpty()) {
                return Pagination.empty();
            }
            profile.ifPresent(p -> query.filter(eq("profile", p)));
        }

        if (publicOnly) {
            query.filter(in("item", mongoItemDao.getPublicItems()));
        }

        if (isNullOrEmpty(queryString)) {
            return getMongoDBUtils().paginationFromQuery(query, offset, count, i -> getMapper().map(i, DistinctInventoryItem.class));
        }

        return getBooleanQueryParser()
                .parse(query, queryString)
                .filter(getMongoDBUtils()::isIndexedQuery)
                .map(q -> getMongoDBUtils().paginationFromQuery(q, offset, count, i -> getMapper().map(i, DistinctInventoryItem.class)))
                .orElse(getMongoDBUtils().paginationFromQuery(query, offset, count, i -> getMapper().map(i, DistinctInventoryItem.class)));

    }

    @Override
    public Long getTotalDistinctInventoryItems(String userId, String profileId, boolean publicOnly, String queryString) {
        final var query = getDatastore().find(MongoDistinctInventoryItem.class);

        if (!isNullOrEmpty(userId)) {
            var user = getMongoUserDao().findMongoUser(userId);
            if (user.isEmpty()) {
                return 0L;
            }
            user.ifPresent(u -> query.filter(eq("user", u)));
        }

        if (!isNullOrEmpty(profileId)) {
            var profile = getMongoProfileDao().findActiveMongoProfile(profileId);
            if (profile.isEmpty()) {
                return 0L;
            }
            profile.ifPresent(p -> query.filter(eq("profile", p)));
        }

        if (publicOnly) {
            query.filter(in("item", mongoItemDao.getPublicItems()));
        }

        if (isNullOrEmpty(queryString)) {
            return getMongoDBUtils().perform(data -> query.count());
        }

        return getBooleanQueryParser()
                .parse(query, queryString)
                .filter(getMongoDBUtils()::isIndexedQuery)
                .map(q -> getMongoDBUtils().perform(data -> query.count()))
                .orElse(0L);
    }

    @Override
    public Long countDistinctMetadataField(final String profileId, final String fieldName){
        final var query = getDatastore().find(MongoDistinctInventoryItem.class);

        if (!isNullOrEmpty(profileId)) {
            var profile = getMongoProfileDao().findActiveMongoProfile(profileId);
            if (profile.isEmpty()) {
                return 0L;
            }
            profile.ifPresent(p -> query.filter(eq("profile", p)));
        }

        query.filter(exists("metadata." + fieldName));

        return getMongoDBUtils().perform(data -> query.stream()
                .map(entry -> entry.getMetadata().get(fieldName))
                .distinct().count());
    }

    @Override
    public DistinctInventoryItem updateDistinctInventoryItem(final DistinctInventoryItem distinctInventoryItem) {

        validationHelper.validateModel(distinctInventoryItem, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrow(
                distinctInventoryItem.getId(),
                DistinctInventoryItemNotFoundException::new);

        final var query = getDatastore()
                .find(MongoDistinctInventoryItem.class)
                .filter(eq("_id", objectId));

        final var mongoItem = getMongoItemDao()
                .findMongoItem(distinctInventoryItem.getItem())
                .orElseThrow(() -> new InvalidDataException("No such item."));

        if (!DISTINCT.equals(mongoItem.getCategory())) {
            var category = ItemCategory.getOrDefault(mongoItem.getCategory());
            throw new InvalidDataException("Invalid item category: " + category);
        }

// See Comment Below
//        final var mongoUser = getMongoUserDao()
//            .findActiveMongoUser(distinctInventoryItem.getUser())
//            .orElseThrow(() -> new InvalidDataException("No such user."));
//
//        final var optionalMongoProfile = Optional.ofNullable(distinctInventoryItem.getProfile())
//            .flatMap(p -> getMongoProfileDao().findActiveMongoProfile(p));

        final var builder = new UpdateBuilder();

        // We aren't able to use modify here because of a bug in 2.1.4. We also can't update to 2.2.0 which fixes the bug
        // because of another bug interfering with other parts of the code. This makes this operation two round trips to the
        // database but there's no other way to do it.
        //
        // https://github.com/MorphiaOrg/morphia/issues/1809 - Issue in 2.1.4
        // https://github.com/MorphiaOrg/morphia/issues/1614 - Issue in 2.2.0

        builder.with(
                set("item", mongoItem),
                distinctInventoryItem.getMetadata() == null
                        ? set("metadata", Map.of())
                        : set("metadata", distinctInventoryItem.getMetadata())
//                ,
//                set("user", mongoUser),
//                optionalMongoProfile
//                    .map(p -> set("profile", p))
//                    .orElseGet(() -> unset("profile"))
        );

        final var options = new ModifyOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        final var result = getMongoDBUtils().perform(ds -> builder.execute(query, options));

        if (result == null) {
            throw new DistinctInventoryItemNotFoundException("No such inventory item.");
        }

        return getMapper().map(result, DistinctInventoryItem.class);

    }

    @Override
    public void deleteDistinctInventoryItem(final String inventoryItemId) {

        final var objectId = getMongoDBUtils().parseOrThrow(
                inventoryItemId,
                DistinctInventoryItemNotFoundException::new);

        final var query = getDatastore()
                .find(MongoDistinctInventoryItem.class)
                .filter(eq("_id", objectId));

        if (query.delete().getDeletedCount() == 0) {
            throw new DistinctInventoryItemNotFoundException("No such inventory item.");
        }

    }

    @Override
    public Optional<DistinctInventoryItem> findDistinctInventoryItemForOwner(
            final String id,
            final String ownerId) {

        final var query = getDatastore().find(MongoDistinctInventoryItem.class);

        final var objectId = getMongoDBUtils().parse(id);

        if (objectId.isEmpty())
            return Optional.empty();

        query.filter(eq("_id", objectId.get()));

        var user = getMongoUserDao().findMongoUser(ownerId);
        var profile  = getMongoProfileDao().findActiveMongoProfile(ownerId);

        if (user.isPresent()) {
            query.filter(eq("user", user.get()));
        } else if (profile.isPresent()) {
            query.filter(eq("profile", profile.get()));
        } else {
            return Optional.empty();
        }

        return Optional
                .ofNullable(query.first())
                .map(u -> getMapper().map(u, DistinctInventoryItem.class));

    }

    public MapperRegistry getMapper() {
        return mapperRegistry;
    }

    @Inject
    public void setMapper(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
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

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
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

    public BooleanQueryParser getBooleanQueryParser() {
        return booleanQueryParser;
    }

    @Inject
    public void setBooleanQueryParser(BooleanQueryParser booleanQueryParser) {
        this.booleanQueryParser = booleanQueryParser;
    }

}