package com.namazustudios.socialengine.dao.mongo.goods;

import com.namazustudios.socialengine.dao.DistinctInventoryItemDao;
import com.namazustudios.socialengine.dao.mongo.MongoProfileDao;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoDistinctInventoryItem;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.inventory.DistinctInventoryItem;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Optional;

public class MongoDistinctInventoryItemDao implements DistinctInventoryItemDao {

    private Mapper mapper;

    private Datastore datastore;

    private MongoItemDao mongoItemDao;

    private MongoUserDao mongoUserDao;

    private MongoProfileDao mongoProfileDao;

    private ValidationHelper validationHelper;

    @Override
    public DistinctInventoryItem createDistinctInventoryItem(final DistinctInventoryItem distinctInventoryItem) {

        getValidationHelper().validateModel(distinctInventoryItem, Insert.class);

        final var mongoDistinctInventoryItem = new MongoDistinctInventoryItem();

        final var mongoItem = getMongoItemDao()
            .findMongoItem(distinctInventoryItem.getId())
            .orElseThrow(() -> new InvalidDataException("No such item."));

        final var mongoUser = getMongoUserDao()
            .findActiveMongoUser(distinctInventoryItem.getUser().getId())
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
    public DistinctInventoryItem getDistinctInventoryItem(final String itemNameOrId) {
        return null;
    }

    @Override
    public Pagination<InventoryItem> getDistinctInventoryItems(final int offset, final int count,
                                                               final String userId, final String profileId) {
        return null;
    }

    @Override
    public DistinctInventoryItem updateDistinctInventoryItem(final DistinctInventoryItem distinctInventoryItem) {
        return null;
    }

    @Override
    public void deleteDistinctInventoryItem(final String inventoryItemId) {

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

}
