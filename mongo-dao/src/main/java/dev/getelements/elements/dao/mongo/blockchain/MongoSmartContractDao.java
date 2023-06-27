package dev.getelements.elements.dao.mongo.blockchain;

import dev.getelements.elements.dao.SmartContractDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.blockchain.MongoSmartContract;
import dev.getelements.elements.dao.mongo.model.blockchain.MongoSmartContractAddress;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.blockchain.SmartContractNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Read;
import dev.getelements.elements.model.ValidationGroups.Update;
import dev.getelements.elements.model.blockchain.BlockchainApi;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.model.blockchain.contract.SmartContract;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.*;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;
import static dev.morphia.query.experimental.updates.UpdateOperators.unset;
import static java.util.stream.Collectors.toList;

public class MongoSmartContractDao implements SmartContractDao {

    private Mapper mapper;

    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

    private MongoVaultDao mongoVaultDao;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<SmartContract> getSmartContracts(
            final int offset, final int count,
            final BlockchainApi blockchainApi,
            final List<BlockchainNetwork> blockchainNetworks) {

        final var query = getDatastore().find(MongoSmartContract.class);

        if (blockchainApi != null) {
            query.filter(elemMatch("addresses", eq("api", blockchainApi)));
        }

        if (blockchainNetworks != null && !blockchainNetworks.isEmpty()) {
            query.filter(elemMatch("addresses", eq("api", in("network", blockchainNetworks))));
        }

        return getMongoDBUtils().paginationFromQuery(query, offset, count, SmartContract.class);

    }

    @Override
    public Optional<SmartContract> findSmartContractById(final String contractNameOrId) {

        final var query = getDatastore().find(MongoSmartContract.class);

        final var objectId = getMongoDBUtils().parse(contractNameOrId);

        if (objectId.isPresent()) {
            query.filter(eq("_id", objectId.get()));
        } else {
            return Optional.empty();
        }

        final var contract = query.first();
        return Optional.ofNullable(contract).map(msc -> getMapper().map(msc, SmartContract.class));

    }

    @Override
    public Optional<SmartContract> findSmartContractByNameOrId(String contractNameOrId) {

        final var query = getDatastore().find(MongoSmartContract.class);

        final var objectId = getMongoDBUtils().parse(contractNameOrId);

        if (objectId.isPresent()) {
            query.filter(eq("_id", objectId.get()));
        } else {
            query.filter(eq("name", contractNameOrId));
        }

        final var contract = query.first();
        return Optional.ofNullable(contract).map(msc -> getMapper().map(msc, SmartContract.class));

    }

    @Override
    public SmartContract updateSmartContract(final SmartContract smartContract) {

        getValidationHelper().validateModel(smartContract, Update.class);
        getValidationHelper().validateModel(smartContract.getVault(), Read.class);

        if (smartContract.getAddresses().keySet().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("All networks must be specified.");
        }

        final var objectId = getMongoDBUtils()
                .parseOrThrow(smartContract.getId(), SmartContractNotFoundException::new);

        final var query = getDatastore()
                .find(MongoSmartContract.class)
                .filter(eq("_id", objectId));

        final var mongoVault = getMongoVaultDao()
                .findMongoVault(smartContract.getVault().getId())
                .orElseThrow(() -> new InvalidDataException("No such vault: " + smartContract.getVault().getId()));

        final var mongoSmartContractAddresses = smartContract
                .getAddresses()
                .entrySet()
                .stream()
                .map(entry -> MongoSmartContractAddress.fromNetworkAndAddress(entry.getKey(), entry.getValue()))
                .collect(toList());

        final var metadata = smartContract.getMetadata();

        final var mongoSmartContract = getMongoDBUtils().perform(ds -> new UpdateBuilder()
                .with(set("name", smartContract.getName()))
                .with(set("displayName", smartContract.getDisplayName().trim()))
                .with(set("vault", mongoVault))
                .with(set("addresses", mongoSmartContractAddresses))
                .with(metadata == null ? unset("metadata") : set("metadata", metadata))
                .execute(query, new ModifyOptions().returnDocument(AFTER).upsert(false))
        );

        if (mongoSmartContract == null) {
            throw new SmartContractNotFoundException();
        }

        return getMapper().map(mongoSmartContract, SmartContract.class);

    }

    @Override
    public SmartContract createSmartContract(final SmartContract smartContract) {

        getValidationHelper().validateModel(smartContract, Insert.class);
        getValidationHelper().validateModel(smartContract.getVault(), Read.class);

        if (smartContract.getAddresses().keySet().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("Must specify non-null networks.");
        }

        final var mongoVault = getMongoVaultDao()
                .findMongoVault(smartContract.getVault().getId())
                .orElseThrow(() -> new InvalidDataException("No such vault: " + smartContract.getVault().getId()));

        final var mongoSmartContract = getMapper().map(smartContract, MongoSmartContract.class);

        mongoSmartContract.setVault(mongoVault);
        getDatastore().insert(mongoSmartContract);

        return getMapper().map(mongoSmartContract, SmartContract.class);

    }

    @Override
    public void deleteContract(final String contractId) {

        final var objectId = getMongoDBUtils().parseOrThrow(contractId, SmartContractNotFoundException::new);
        final var query = getDatastore().find(MongoSmartContract.class);
        final var result = query.filter(eq("_id", objectId)).delete();

        if (result.getDeletedCount() == 0) {
            throw new SmartContractNotFoundException();
        }

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

    public MongoVaultDao getMongoVaultDao() {
        return mongoVaultDao;
    }

    @Inject
    public void setMongoVaultDao(MongoVaultDao mongoVaultDao) {
        this.mongoVaultDao = mongoVaultDao;
    }

}
