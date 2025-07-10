package dev.getelements.elements.dao.mongo.blockchain;

import dev.getelements.elements.sdk.dao.VaultDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.blockchain.MongoVault;
import dev.getelements.elements.dao.mongo.model.blockchain.MongoVaultKey;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.blockchain.VaultNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Read;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import jakarta.inject.Inject;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.and;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoVaultDao implements VaultDao {

    private MapperRegistry mapperRegistry;

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoDBUtils mongoDBUtils;

    private MongoWalletDao mongoWalletDao;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<Vault> getVaults(final int offset, final int count, final String userId) {

        final var query = getDatastore().find(MongoVault.class);

        if (userId != null && !userId.isBlank()) {

            final var user = getMongoUserDao().findMongoUser(userId);

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

            final var user = getMongoUserDao().findMongoUser(userId);

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

        if (getMongoUserDao().findMongoUser(mongoUserId).isEmpty()) {
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
            .findMongoUser(vault.getUser().getId())
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
    public Optional<Vault> findAndUpdateVaultBelongingToUser(final Vault vault, final String userId) {

        getValidationHelper().validateModel(vault, Update.class);
        getValidationHelper().validateModel(vault.getUser(), Read.class);

        final var mongoVaultKey = getMapper().map(vault.getKey(), MongoVaultKey.class);
        final var objectId = getMongoDBUtils().parseOrThrow(vault.getId(), VaultNotFoundException::new);

        final var mongoUser = getMongoUserDao().findMongoUser(userId);

        if (mongoUser.isEmpty()) {
            return Optional.empty();
        }

        final var query = getDatastore()
                .find(MongoVault.class)
                .filter(and(
                        eq("_id", objectId),
                        eq("user", mongoUser.get())
                ));

        final var updated = new UpdateBuilder()
                .with(set("user", mongoUser.get()))
                .with(set("displayName", vault.getDisplayName()))
                .with(set("key", mongoVaultKey))
                .execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER));

        if (updated == null) {
            return Optional.empty();
        } else {
            final var mapped = getMapper().map(updated, Vault.class);
            return Optional.of(mapped);
        }

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
                .findMongoUser(userId)
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
