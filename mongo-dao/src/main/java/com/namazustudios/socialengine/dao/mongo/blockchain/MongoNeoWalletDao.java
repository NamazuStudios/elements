package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoWallet;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.or;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoNeoWalletDao implements NeoWalletDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    private MongoUserDao mongoUserDao;

    private MongoApplicationDao mongoApplicationDao;

    @Override
    public Pagination<NeoWallet> getWallets(final int offset,
                                            final int count,
                                            final String userId) {

        var query = getDatastore().find(MongoNeoWallet.class);
        final var mongoUser = getMongoUserDao().getActiveMongoUser(userId);

        query.filter(eq("user", mongoUser));

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> transform(input), new FindOptions());
    }

    @Override
    public Optional<NeoWallet> getWallet(String walletIdOrName) {

        final var query = getDatastore().find(MongoNeoWallet.class);

        query.filter(or(
                eq("_id", new ObjectId(walletIdOrName)),
                eq("displayName", true)
        ));

        final var mongoNeoWallet = query.first();
        return mongoNeoWallet == null ? Optional.empty() : Optional.of(transform(mongoNeoWallet));
    }

    @Override
    public NeoWallet updateWallet(NeoWallet wallet) {
        getValidationHelper().validateModel(wallet, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(wallet.getId());
        final var query = getDatastore().find(MongoNeoWallet.class);

        final var builder = new UpdateBuilder();

        query.filter(eq("_id", objectId));

        builder.with(
                set("displayName", nullToEmpty(wallet.getDisplayName()).trim())
        );

        final MongoNeoWallet mongoNeoWallet = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoNeoWallet == null) {
            throw new NotFoundException("application not found: " + wallet.getId());
        }

        getObjectIndex().index(mongoNeoWallet);
        return transform(mongoNeoWallet);
    }

    @Override
    public NeoWallet createWallet(NeoWallet wallet) {

        getValidationHelper().validateModel(wallet, ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoNeoWallet.class);
        final var user = getMongoUserFromWallet(wallet);

        query.filter(eq("user", user));

        // These fields are not part of the "refresh" operation, but will get set if it is the first time we create
        // the wallet.
        final var insertMap = new HashMap<String, Object>(Collections.emptyMap());

        insertMap.put("user", user);
        insertMap.put("displayName", nullToEmpty(wallet.getDisplayName()).trim());

        final var builder = new UpdateBuilder();

        final var mongoWallet = getMongoDBUtils().perform(ds ->
                builder.with(
                        UpdateOperators.setOnInsert(insertMap)
                ).execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoWallet);
        return transform(mongoWallet);
    }

    @Override
    public void deleteWallet(final String walletId) {

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(walletId);
        final var query = getDatastore().find(MongoNeoWallet.class);

        query.filter(eq("_id", objectId));
        query.delete();
    }


    public NeoWallet transform(final MongoNeoWallet input) {

        if (!input.getUser().isActive()) {
            input.setUser(null);
        }

        return getBeanMapper().map(input, NeoWallet.class);

    }

    private MongoUser getMongoUserFromWallet(final NeoWallet wallet) {
        return getMongoUserDao().getActiveMongoUser(wallet.getUser().getId());
    }


    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

}
