package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.BscWalletDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoBscWallet;
import com.namazustudios.socialengine.exception.blockchain.BscWalletNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.Web3jWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;
import com.namazustudios.socialengine.dao.mongo.converter.MongoBscWalletConverter;

import javax.inject.Inject;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.and;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoBscWalletDao implements BscWalletDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    private MongoUserDao mongoUserDao;

    private MongoApplicationDao mongoApplicationDao;



    @Override
    public Pagination<BscWallet> getWallets(final int offset,
                                            final int count,
                                            final String userId) {

        var query = getDatastore().find(MongoBscWallet.class);

        if (!nullToEmpty(userId).trim().isEmpty()) {
            final var mongoUser = getMongoUserDao().getActiveMongoUser(userId);
            query.filter(eq("user", mongoUser));
        }

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> transform(input), new FindOptions());
    }

    @Override
    public BscWallet getWallet(String walletNameOrId) {

        final var objectId = getMongoDBUtils().parseOrReturnNull(walletNameOrId);

        var mongoBscWallet = getDatastore().find(MongoBscWallet.class)
                .filter(Filters.or(
                                Filters.eq("_id", objectId),
                                Filters.eq("displayName", walletNameOrId)
                        )
                ).first();

        if (mongoBscWallet == null) {
            throw new BscWalletNotFoundException("Wallet not found: " + walletNameOrId);
        }

        return transform(mongoBscWallet);
    }

    @Override
    public BscWallet getWalletForUser(String userId, String walletName){

        final var query = getDatastore().find(MongoBscWallet.class);
        final var user = getMongoUser(userId);

        query.filter(and(
                eq("user", user),
                eq("displayName", walletName)
        ));

        final var mongoBscWallet = query.first();
        return mongoBscWallet == null ? null : transform(mongoBscWallet);
    }

    @Override
    public BscWallet updateWallet(String walletId, UpdateBscWalletRequest updatedWalletRequest, Web3jWallet updatedWallet) throws JsonProcessingException {
        getValidationHelper().validateModel(updatedWalletRequest, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(walletId);
        final var query = getDatastore().find(MongoBscWallet.class);
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

        builder.with(set("wallet", MongoBscWalletConverter.OBJECT_MAPPER.writeValueAsBytes(updatedWallet)));

        final MongoBscWallet mongoBscWallet = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoBscWallet == null) {
            throw new BscWalletNotFoundException("Wallet not found: " + walletId);
        }

        getObjectIndex().index(mongoBscWallet);
        return transform(mongoBscWallet);
    }

    @Override
    public BscWallet createWallet(BscWallet wallet) throws JsonProcessingException {

        getValidationHelper().validateModel(wallet, ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoBscWallet.class);
        final var user = getMongoUser(wallet.getUser().getId());

        query.filter(and(
                eq("user", user),
                eq("displayName", nullToEmpty(wallet.getDisplayName()).trim())
        ));

        final var builder = new UpdateBuilder().with(
                set("user", user),
                set("displayName", nullToEmpty(wallet.getDisplayName()).trim()),
                set("wallet", MongoBscWalletConverter.OBJECT_MAPPER.writeValueAsBytes(wallet.getWallet()))
        );

        final var mongoWallet = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoWallet);
        return transform(mongoWallet);
    }

    @Override
    public void deleteWallet(final String walletId) {
        final var objectId = getMongoDBUtils().parseOrThrow(walletId, BscWalletNotFoundException::new);

        final var result = getDatastore()
                .find(MongoBscWallet.class)
                .filter(eq("_id", objectId))
                .delete();

        if(result.getDeletedCount() == 0){
            throw new BscWalletNotFoundException("BscWallet not deleted: " + walletId);
        }
    }


    public BscWallet transform(final MongoBscWallet input) {

        if (!input.getUser().isActive()) {
            input.setUser(null);
        }

        return getBeanMapper().map(input, BscWallet.class);
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
