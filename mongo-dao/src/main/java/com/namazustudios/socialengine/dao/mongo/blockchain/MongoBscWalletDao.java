package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.BscWalletDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.converter.MongoBscWalletConverter;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoBscWallet;
import com.namazustudios.socialengine.exception.blockchain.BscWalletNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;

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

        return getMongoDBUtils().paginationFromQuery(query, offset, count, this::transform, new FindOptions());

    }

    @Override
    public BscWallet getWallet(final String walletId) {

        final var objectId = getMongoDBUtils().parseOrReturnNull(walletId);

        var mongoBscWallet = getDatastore()
            .find(MongoBscWallet.class)
            .filter(Filters.eq("_id", objectId))
            .first();

        if (mongoBscWallet == null) {
            throw new BscWalletNotFoundException("Wallet not found: " + walletId);
        }

        return transform(mongoBscWallet);

    }

    @Override
    public BscWallet getWalletForUser(final String userId, final String walletId) {

        final var query = getDatastore().find(MongoBscWallet.class);
        final var user = getMongoUserDao().getActiveMongoUser(userId);
        final var objectId = getMongoDBUtils().parseOrThrow(walletId, BscWalletNotFoundException::new);

        query.filter(and(
            eq("_id", objectId),
            eq("user", user)
        ));

        final var mongoBscWallet = query.first();

        if (mongoBscWallet == null) {
            throw new BscWalletNotFoundException("Wallet not found: " + walletId);
        }

        return transform(mongoBscWallet);

    }

    @Override
    public BscWallet updateWallet(final BscWallet bscWallet) {

        getValidationHelper().validateModel(bscWallet, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrow(
            bscWallet.getId(),
            BscWalletNotFoundException::new
        );

        final var displayName = nullToEmpty(bscWallet.getDisplayName()).trim();
        final var mongoUser = getMongoUserDao().getActiveMongoUser(bscWallet.getUser());

        final var builder = new UpdateBuilder()
            .with(set("user", mongoUser))
            .with(set("displayName", displayName))
            .with(set("wallet", MongoBscWalletConverter.asBytes(bscWallet.getWallet())));

        final var query = getDatastore()
                .find(MongoBscWallet.class)
                .filter(eq("_id", objectId));

        final MongoBscWallet mongoBscWallet = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoBscWallet == null) {
            throw new BscWalletNotFoundException("Wallet not found: " + objectId);
        }

        getObjectIndex().index(mongoBscWallet);
        return transform(mongoBscWallet);

    }

    @Override
    public BscWallet createWallet(final BscWallet wallet) {

        getValidationHelper().validateModel(wallet, ValidationGroups.Insert.class);

        final var mongoUser = getMongoUserDao().getActiveMongoUser(wallet.getUser().getId());

        final var mongoBscWallet = new MongoBscWallet();
        mongoBscWallet.setUser(mongoUser);
        mongoBscWallet.setDisplayName(wallet.getDisplayName());
        mongoBscWallet.setWallet(MongoBscWalletConverter.asBytes(wallet.getWallet()));

        final var saved = getMongoDBUtils().perform(ds -> ds.save(mongoBscWallet));
        getObjectIndex().index(saved);

        return transform(saved);

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
