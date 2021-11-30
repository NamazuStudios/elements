package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoWallet;
import com.namazustudios.socialengine.exception.blockchain.NeoWalletNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;
import com.namazustudios.socialengine.model.blockchain.UpdateNeoWalletRequest;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import io.neow3j.wallet.Wallet;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Base64;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.*;
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

        if (!nullToEmpty(userId).trim().isEmpty()) {
            final var mongoUser = getMongoUserDao().getActiveMongoUser(userId);
            query.filter(eq("user", mongoUser));
        }

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> transform(input), new FindOptions());
    }

    @Override
    public NeoWallet getWallet(String walletId) {

        final var query = getDatastore().find(MongoNeoWallet.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(walletId);

        query.filter(eq("_id", objectId));

        final var mongoNeoWallet = query.first();

        if (mongoNeoWallet == null) {
            throw new NeoWalletNotFoundException("Wallet not found: " + walletId);
        }

        return transform(mongoNeoWallet);
    }

    @Override
    public NeoWallet getWalletForUser(String userId, String walletName){

        final var query = getDatastore().find(MongoNeoWallet.class);
        final var user = getMongoUser(userId);

        query.filter(and(
                eq("user", user),
                eq("displayName", walletName)
        ));

        final var mongoNeoWallet = query.first();
        return mongoNeoWallet == null ? null : transform(mongoNeoWallet);
    }

    @Override
    public NeoWallet updateWallet(String walletId, UpdateNeoWalletRequest updatedWalletRequest) {

        getValidationHelper().validateModel(updatedWalletRequest, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(walletId);
        final var query = getDatastore().find(MongoNeoWallet.class);
        final var displayName = nullToEmpty(updatedWalletRequest.getDisplayName()).trim();
        final var newUserId = nullToEmpty(updatedWalletRequest.getNewUserId()).trim();

        final var builder = new UpdateBuilder();

        query.filter(eq("_id", objectId));

        if (!displayName.isEmpty()) {
            builder.with(set("displayName", displayName));
        }
        if (!newUserId.isEmpty()){
            final var newUser = getMongoUser(newUserId);
            builder.with(set("user", newUser));
        }

        builder.with(set("walletString", updatedWalletRequest.getUpdatedWallet()));

        final MongoNeoWallet mongoNeoWallet = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoNeoWallet == null) {
            throw new NeoWalletNotFoundException("Wallet not found: " + walletId);
        }

        getObjectIndex().index(mongoNeoWallet);
        return transform(mongoNeoWallet);
    }

    @Override
    public NeoWallet createWallet(NeoWallet wallet) throws JsonProcessingException {

        getValidationHelper().validateModel(wallet, ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoNeoWallet.class);
        final var user = getMongoUser(wallet.getUser().getId());
        final var walletBytes = Wallet.OBJECT_MAPPER.writeValueAsBytes(wallet.getWallet());

        query.filter(and(
                eq("user", user),
                eq("displayName", nullToEmpty(wallet.getDisplayName()).trim())
        ));

        final var builder = new UpdateBuilder().with(
                set("user", user),
                set("displayName", nullToEmpty(wallet.getDisplayName()).trim()),
                set("walletString", Base64.getEncoder().encodeToString(walletBytes))
        );

        final var mongoWallet = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
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

    private MongoUser getMongoUser(final String userId) {
        return getMongoUserDao().getActiveMongoUser(userId);
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
