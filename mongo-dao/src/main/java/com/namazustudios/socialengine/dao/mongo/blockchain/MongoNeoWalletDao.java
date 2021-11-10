package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoFollower;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoWallet;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;
import com.namazustudios.socialengine.model.blockchain.UpdateWalletRequest;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.updates.UpdateOperators;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.nep6.NEP6Wallet;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

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
        final var mongoUser = getMongoUserDao().getActiveMongoUser(userId);

        query.filter(eq("user", mongoUser));

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> transform(input), new FindOptions());
    }

    @Override
    public Optional<NeoWallet> getWallet(String walletId) {

        final var query = getDatastore().find(MongoNeoWallet.class);

        query.filter(eq("_id", new ObjectId(walletId)));

        final var mongoNeoWallet = query.first();
        return mongoNeoWallet == null ? Optional.empty() : Optional.of(transform(mongoNeoWallet));
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
    public NeoWallet updateWallet(String walletId, String userId, NEP6Wallet updatedWallet) throws JsonProcessingException {

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(walletId);
        final var query = getDatastore().find(MongoNeoWallet.class);
        final var walletBytes = Wallet.OBJECT_MAPPER.writeValueAsBytes(updatedWallet);
        final var displayName = nullToEmpty(updatedWallet.getName()).trim();
        final var user = getMongoUser(userId);

        final var builder = new UpdateBuilder();

        query.filter(eq("_id", objectId));

        if (!displayName.isEmpty()) {
            builder.with(set("displayName", nullToEmpty(updatedWallet.getName()).trim()));
        }

        builder.with(
                set("user", user),
                set("walletString", Base64.getEncoder().encodeToString(walletBytes))
        );

        final MongoNeoWallet mongoNeoWallet = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoNeoWallet == null) {
            throw new NotFoundException("Wallet not found: " + walletId);
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
