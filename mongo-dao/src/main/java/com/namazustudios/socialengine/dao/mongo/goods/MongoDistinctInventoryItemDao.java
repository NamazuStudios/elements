package com.namazustudios.socialengine.dao.mongo.goods;

import com.mongodb.client.model.ReturnDocument;
import com.namazustudios.socialengine.dao.DistinctInventoryItemDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoProfileDao;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoDistinctInventoryItem;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.inventory.DistinctInventoryItemNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.inventory.DistinctInventoryItem;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Optional;

import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoDistinctInventoryItemDao implements DistinctInventoryItemDao {

    private Mapper mapper;

    private Datastore datastore;

    private MongoItemDao mongoItemDao;

    private MongoUserDao mongoUserDao;

    private MongoProfileDao mongoProfileDao;

    private ValidationHelper validationHelper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public DistinctInventoryItem createDistinctInventoryItem(final DistinctInventoryItem distinctInventoryItem) {

        getValidationHelper().validateModel(distinctInventoryItem, Insert.class);

        final var mongoDistinctInventoryItem = new MongoDistinctInventoryItem();

        final var mongoItem = getMongoItemDao()
            .findMongoItem(distinctInventoryItem.getItem())
            .orElseThrow(() -> new InvalidDataException("No such item."));

        final var mongoUser = getMongoUserDao()
            .findActiveMongoUser(distinctInventoryItem.getUser())
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
            final String userId, final String profileId) {

        final var query = getDatastore().find(MongoDistinctInventoryItem.class);

        final var user = userId == null
            ? Optional.empty()
            : getMongoUserDao().findActiveMongoUser(userId);

        final var profile = profileId == null
            ? Optional.empty()
            : getMongoProfileDao().findActiveMongoProfile(profileId);

        user.ifPresent(u -> query.filter(eq("user", u)));
        profile.ifPresent(p -> query.filter(eq("profile", p)));

        return getMongoDBUtils().paginationFromQuery(query, offset, count, i -> getMapper().map(i, DistinctInventoryItem.class));

    }

    @Override
    public DistinctInventoryItem updateDistinctInventoryItem(final DistinctInventoryItem distinctInventoryItem) {

        final var objectId = getMongoDBUtils().parseOrThrow(
                distinctInventoryItem.getId(),
                DistinctInventoryItemNotFoundException::new);

        final var query = getDatastore()
            .find(MongoDistinctInventoryItem.class)
            .filter(eq("_id", objectId));

        final var builder = new UpdateBuilder();

        getMongoItemDao()
            .findMongoItem(distinctInventoryItem.getItem())
            .map(i -> builder.with(set("item", i)))
            .orElseThrow(() -> new InvalidDataException("No such item."));

        getMongoUserDao()
            .findActiveMongoUser(distinctInventoryItem.getUser())
            .map(u -> builder.with(set("user", u)))
            .orElseThrow(() -> new InvalidDataException("No such item."));

        Optional.ofNullable(distinctInventoryItem.getProfile())
            .flatMap(p -> getMongoProfileDao().findActiveMongoProfile(p))
            .map(p -> builder.with(set("profile", p)))
            .orElse(builder)
            .with(set("metadata", distinctInventoryItem.getMetadata()));

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

    public Mapper getMapper() {
        return mapper;
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
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

}
