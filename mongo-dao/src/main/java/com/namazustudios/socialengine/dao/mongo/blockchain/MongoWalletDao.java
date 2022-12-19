package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoWallet;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.blockchain.WalletNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.in;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;


public class MongoWalletDao implements WalletDao {

    private Mapper mapper;

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoDBUtils mongoDBUtils;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<Wallet> getWallets(
            final int offset, final int count,
            final String userId,
            final BlockchainApi protocol,
            final List<BlockchainNetwork> networks) {

        final var query = getDatastore().find(MongoWallet.class);

        if (userId != null) {

            final var mongoUser = getMongoUserDao().findActiveMongoUser(userId);

            if (mongoUser.isEmpty())
                return Pagination.empty();

            query.filter(eq("user", mongoUser.get()));

        }

        if (protocol != null)
            query.filter(eq("protocol", protocol));

        if (networks != null) {
            if (networks.isEmpty()) return Pagination.empty();
            networks.forEach(n -> query.filter(in("networks", List.of(n))));
        }

        return getMongoDBUtils().paginationFromQuery(query, offset, count, Wallet.class);

    }

    @Override
    public Optional<Wallet> findWallet(final String walletId, final String userId) {
        return findMongoWallet(walletId, userId).map(mw -> getMapper().map(mw, Wallet.class));
    }

    public Optional<MongoWallet> findMongoWallet(final String walletId, final String userId) {
        return getMongoDBUtils()
                .parse(walletId)
                .flatMap(oid -> {

                    final var query = getDatastore().find(MongoWallet.class);
                    query.filter(Filters.eq("_id", oid));

                    if (userId != null) {

                        final var user = getMongoUserDao().findActiveMongoUser(userId);

                        if (user.isEmpty())
                            return Optional.empty();

                        query.filter(eq("user", user.get()));

                    }

                    return Optional.ofNullable(query.first());

                });
    }

    @Override
    public Wallet updateWallet(final Wallet wallet) {

        getValidationHelper().validateModel(wallet, ValidationGroups.Update.class);

        if (wallet.getIdentities().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("All identities must be specified.");
        }

        if (wallet.getNetworks().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("All networks must be specified.");
        }

        final var protocols = EnumSet.noneOf(BlockchainApi.class);
        wallet.getNetworks().forEach(net -> protocols.add(net.api()));

        if (protocols.size() > 1 && protocols.contains(wallet.getApi())) {
            throw new InvalidDataException("All networks must use the same protocol and must match: " + wallet.getApi());
        }

        final var objectId = getMongoDBUtils().parseOrThrow(wallet.getId(), WalletNotFoundException::new);

        final var query = getDatastore().find(MongoWallet.class);
        query.filter(eq("_id", objectId));

        final var mongoUser = getMongoUserDao()
                .findActiveMongoUser(wallet.getUser())
                .orElseThrow(() -> new InvalidDataException("No such user: " + wallet.getUser().getId()));

        final var mongoWallet = new UpdateBuilder()
                .with(set("user", mongoUser))
                .with(set("displayName", wallet.getDisplayName().trim()))
                .with(set("api", wallet.getApi()))
                .with(set("networks", wallet.getNetworks()))
                .with(set("defaultIdentity", wallet.getDefaultIdentity()))
                .with(set("identities", wallet.getIdentities()))
                .with(set("encryption", wallet.getEncryption()))
                .execute(query, new ModifyOptions().returnDocument(AFTER).upsert(false));

        if (mongoWallet == null) {
            throw new WalletNotFoundException();
        }

        return getMapper().map(mongoWallet, Wallet.class);

    }

    @Override
    public Wallet createWallet(final Wallet wallet) {

        getValidationHelper().validateModel(wallet, ValidationGroups.Insert.class);
        getValidationHelper().validateModel(wallet.getUser(), ValidationGroups.Update.class);

        if (wallet.getIdentities().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("All identities must be specified.");
        }

        if (wallet.getNetworks().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("All networks must be specified.");
        }

        final var protocols = EnumSet.noneOf(BlockchainApi.class);
        wallet.getNetworks().forEach(net -> protocols.add(net.api()));

        if (protocols.size() > 1 && protocols.contains(wallet.getApi())) {
            throw new InvalidDataException("All networks must use the same protocol and must match: " + wallet.getApi());
        }

        final var mongoWallet = getMapper().map(wallet, MongoWallet.class);

        final var mongoUser = getMongoUserDao()
                .findActiveMongoUser(wallet.getUser())
                .orElseThrow(() -> new InvalidDataException("No such user: " + wallet.getUser().getId()));

        mongoWallet.setUser(mongoUser);
        getDatastore().insert(mongoWallet);

        return getMapper().map(mongoWallet, Wallet.class);

    }

    @Override
    public void deleteWallet(final String walletId, final String userId) {
        deleteWallet(walletId, userId, null);
    }

    public void deleteWallet(final String walletId, final String userId, final BlockchainApi blockchainApi) {

        final var objectId = getMongoDBUtils().parseOrThrow(walletId, WalletNotFoundException::new);

        final var query = getDatastore()
                .find(MongoWallet.class)
                .filter(eq("_id", objectId));

        if (userId != null) {

            final var mongoUser = getMongoUserDao().findActiveMongoUser(userId);

            if (mongoUser.isEmpty())
                return;

            query.filter(eq("user", mongoUser.get()));

        }

        if (blockchainApi != null)
            query.filter(eq("protocol", blockchainApi));

        final var result = query.delete();

        if (result.getDeletedCount() == 0)
            throw new WalletNotFoundException();

    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
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

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
