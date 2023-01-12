package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.socialengine.dao.VaultDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoVault;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoVaultKey;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.blockchain.VaultNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Read;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoVaultDao implements VaultDao {

    private Mapper mapper;

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoDBUtils mongoDBUtils;

    private MongoWalletDao mongoWalletDao;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<Vault> getVaults(final int offset, final int count, final String userId) {

        final var query = getDatastore().find(MongoVault.class);

        if (userId != null && !userId.isBlank()) {

            final var user = getMongoUserDao().findActiveMongoUser(userId);

            if (user.isEmpty()) {
                return Pagination.empty();
            } else {
                query.filter(eq("user", user.get()));
            }

        }

        return getMongoDBUtils().paginationFromQuery(query, offset, count, v -> getMapper().map(v, Vault.class));

    }

    @Override
    public Optional<Vault> findVault(final String vaultId) {
        return findMongoVault(vaultId).map(v -> getMapper().map(v, Vault.class));
    }

    @Override
    public Optional<Vault> findVaultForUser(final String vaultId, final String userId) {
        return findMongoVaultForUser(vaultId, userId).map(v -> getMapper().map(v, Vault.class));
    }

    public MongoVault getMongoVault(final String vaultId) {
        return findMongoVault(vaultId).orElseThrow(VaultNotFoundException::new);
    }

    public Optional<MongoVault> findMongoVault(final String vaultId) {

        final var objectId = getMongoDBUtils().parse(vaultId);

        if (objectId.isEmpty())
            return Optional.empty();

        final var query = getDatastore()
                .find(MongoVault.class)
                .filter(eq("_id", objectId.get()));

        final var mongoVault = query.first();
        return Optional.ofNullable(mongoVault);

    }

    public Optional<MongoVault> findMongoVaultForUser(final String vaultId, final String userId) {

        final var objectId = getMongoDBUtils().parse(vaultId);

        if (objectId.isEmpty())
            return Optional.empty();

        final var query = getDatastore()
                .find(MongoVault.class)
                .filter(eq("_id", objectId.get()));

        if (userId != null && !userId.isBlank()) {

            final var user = getMongoUserDao().findActiveMongoUser(userId);

            if (user.isEmpty()) {
                return Optional.empty();
            }

            query.filter(eq("user", user.get()));

        }

        final var mongoVault = query.first();
        return Optional.ofNullable(mongoVault);

    }

    @Override
    public Vault createVault(final Vault vault) {

        getValidationHelper().validateModel(vault, Insert.class);
        getValidationHelper().validateModel(vault.getUser(), Update.class);

        final var mongoVault = getMapper().map(vault, MongoVault.class);
        final var mongoUserId = mongoVault.getUser().getObjectId();

        if (getMongoUserDao().findActiveMongoUser(mongoUserId).isEmpty()) {
            throw new InvalidDataException("No user with id: " + mongoUserId);
        }

        getDatastore().insert(mongoVault);
        return getMapper().map(mongoVault, Vault.class);

    }

    @Override
    public Vault updateVault(final Vault vault) {

        getValidationHelper().validateModel(vault, Update.class);
        getValidationHelper().validateModel(vault.getUser(), Read.class);

        final var mongoUser = getMongoUserDao()
            .findActiveMongoUser(vault.getUser())
            .orElseThrow(() -> new InvalidDataException("User not found: " + vault.getUser().getId()));

        final var mongoVaultKey = getMapper().map(vault.getKey(), MongoVaultKey.class);

        final var objectId = getMongoDBUtils().parseOrThrow(vault.getId(), VaultNotFoundException::new);

        final var query = getDatastore()
                .find(MongoVault.class)
                .filter(eq("_id", objectId));

        final var updated = new UpdateBuilder()
                .with(set("user", mongoUser))
                .with(set("displayName", vault.getDisplayName()))
                .with(set("key", mongoVaultKey))
                .execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER));

        if (updated == null) {
            throw new VaultNotFoundException();
        }

        return getMapper().map(updated, Vault.class);

    }

    @Override
    public void deleteVault(final String vaultId) {

        final var objectId = getMongoDBUtils().parseOrThrow(vaultId, VaultNotFoundException::new);

        final var deleted = getDatastore()
                .find(MongoVault.class)
                .filter(eq("_id", objectId))
                .findAndDelete();

        if (deleted == null) {
            throw new VaultNotFoundException();
        }

        getMongoWalletDao().deleteWalletsInMongoVault(deleted);

    }

    @Override
    public void deleteVaultForUser(final String vaultId, final String userId) {

        final var objectId = getMongoDBUtils().parseOrThrow(vaultId, VaultNotFoundException::new);

        final var mongoUser = getMongoUserDao()
                .findActiveMongoUser(userId)
                .orElseThrow(VaultNotFoundException::new);

        final var deleted = getDatastore()
                .find(MongoVault.class)
                .filter(eq("_id", objectId))
                .filter(eq("user", mongoUser))
                .findAndDelete();

        if (deleted == null) {
            throw new VaultNotFoundException();
        }

        getMongoWalletDao().deleteWalletsInMongoVault(deleted);

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

    public MongoWalletDao getMongoWalletDao() {
        return mongoWalletDao;
    }

    @Inject
    public void setMongoWalletDao(MongoWalletDao mongoWalletDao) {
        this.mongoWalletDao = mongoWalletDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
