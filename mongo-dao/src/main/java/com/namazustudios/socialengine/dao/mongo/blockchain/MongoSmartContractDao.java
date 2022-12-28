package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.socialengine.dao.SmartContractDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoSmartContract;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoWallet;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import dev.morphia.Datastore;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.in;

public class MongoSmartContractDao implements SmartContractDao {

    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

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
        return Optional.empty();
    }

    @Override
    public SmartContract updateSmartContract(final SmartContract smartContract) {
        return null;
    }

    @Override
    public SmartContract createSmartContract(final SmartContract smartContract) {
        return null;
    }

    @Override
    public void deleteContract(final String contractId) {

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
}
