package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.socialengine.dao.SmartContractDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoSmartContract;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.blockchain.SmartContractNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.*;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.in;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;
import static java.lang.String.format;

public class MongoSmartContractDao implements SmartContractDao {

    private Mapper mapper;

    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

    private ValidationHelper validationHelper;

    private MongoWalletDao mongoWalletDao;

    @Override
    public Pagination<SmartContract> getSmartContracts(
            final int offset, final int count,
            final BlockchainApi blockchainApi,
            final List<BlockchainNetwork> blockchainNetworks) {

        final var query = getDatastore().find(MongoSmartContract.class);

        if (blockchainApi != null)
            query.filter(eq("api", blockchainApi));

        if (blockchainNetworks != null) {
            if (blockchainNetworks.isEmpty()) return Pagination.empty();
            blockchainNetworks.forEach(n -> query.filter(in("networks", List.of(n))));
        }

        return getMongoDBUtils().paginationFromQuery(query, offset, count, SmartContract.class);

    }

    @Override
    public Optional<SmartContract> findSmartContract(final String contractId) {

        final var objectId = getMongoDBUtils().parseOrThrow(contractId, SmartContractNotFoundException::new);

        final var query = getDatastore()
                .find(MongoSmartContract.class)
                .filter(eq("_id", objectId));

        final var contract = query.first();

        return Optional.ofNullable(contract).map(msc -> getMapper().map(msc, SmartContract.class));

    }

    @Override
    public SmartContract updateSmartContract(final SmartContract smartContract) {

        getValidationHelper().validateModel(smartContract, ValidationGroups.Update.class);
        getValidationHelper().validateModel(smartContract.getWallet(), ValidationGroups.Update.class);

        if (smartContract.getAddresses().keySet().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("All networks must be specified.");
        }

        final var protocols = EnumSet.noneOf(BlockchainApi.class);
        smartContract.getAddresses().keySet().forEach(net -> protocols.add(net.api()));

        if (protocols.size() > 1 && protocols.contains(smartContract.getApi())) {
            throw new InvalidDataException("All networks must use the same protocol and must match: " + smartContract.getApi());
        }

        final var objectId = getMongoDBUtils().parseOrThrow(smartContract.getId(), SmartContractNotFoundException::new);

        final var query = getDatastore().find(MongoSmartContract.class);
        query.filter(eq("_id", objectId));

        final var mongoWallet = getMongoWalletDao()
                .findMongoWallet(smartContract.getWallet().getId())
                .orElseThrow(() -> new InvalidDataException("No such wallet: " + smartContract.getWallet().getId()));

        if (!smartContract.getApi().equals(mongoWallet.getApi())) {
            final var msg = format("%s=/=%s", smartContract.getApi(), mongoWallet.getApi());
            throw new InvalidDataException(msg);
        }

        final var networks = new ArrayList<>(smartContract.getAddresses().values());

        mongoWallet.getApi().validate(smartContract.getAddresses().keySet());

        final var mongoSmartContract = new UpdateBuilder()
                .with(set("displayName", smartContract.getDisplayName().trim()))
                .with(set("api", smartContract.getApi()))
                .with(set("addresses", smartContract.getAddresses()))
                .with(set("wallet", mongoWallet))
                .with(set("networks", smartContract.getAddresses().keySet()))
                .with(set("metadata", smartContract.getMetadata()))
                .execute(query, new ModifyOptions().returnDocument(AFTER).upsert(false));

        if (mongoSmartContract == null) {
            throw new SmartContractNotFoundException();
        }

        return getMapper().map(mongoWallet, SmartContract.class);

    }

    @Override
    public SmartContract createSmartContract(final SmartContract smartContract) {

        getValidationHelper().validateModel(smartContract, ValidationGroups.Insert.class);
        getValidationHelper().validateModel(smartContract.getWallet(), ValidationGroups.Update.class);

        if (smartContract.getAddresses().keySet().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("Must specify non-null networks.");
        }

        smartContract.getApi().validate(smartContract.getAddresses().keySet());

        final var mongoWallet = getMongoWalletDao()
                .findMongoWallet(smartContract.getWallet().getId())
                .orElseThrow(() -> new InvalidDataException("No such wallet: " + smartContract.getWallet().getId()));

        if (!smartContract.getApi().equals(mongoWallet.getApi())) {
            final var msg = format("%s=/=%s", smartContract.getApi(), mongoWallet.getApi());
            throw new InvalidDataException(msg);
        }

        mongoWallet.getApi().validate(smartContract.getAddresses().keySet());

        final var mongoSmartContract = getMapper().map(smartContract, MongoSmartContract.class);
        final var networks = new ArrayList<>(smartContract.getAddresses().keySet());
        mongoSmartContract.setNetworks(networks);

        mongoSmartContract.setWallet(mongoWallet);
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

    public MongoWalletDao getMongoWalletDao() {
        return mongoWalletDao;
    }

    @Inject
    public void setMongoWalletDao(MongoWalletDao mongoWalletDao) {
        this.mongoWalletDao = mongoWalletDao;
    }

}
