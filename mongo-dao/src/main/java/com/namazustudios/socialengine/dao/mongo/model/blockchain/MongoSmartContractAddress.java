package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContractAddress;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.util.Objects;

@Embedded
public class MongoSmartContractAddress {

    public MongoSmartContractAddress() {}

    public MongoSmartContractAddress(final SmartContractAddress from) {
        this.address = from.getAddress();
    }

    @Property
    private String address;

    @Property
    private BlockchainApi api;

    @Property
    private BlockchainNetwork network;

    public static MongoSmartContractAddress fromNetworkAndAddress(
            final BlockchainNetwork network,
            final SmartContractAddress smartContractAddress) {
        final var mongoSmartContractAddress = new MongoSmartContractAddress();
        mongoSmartContractAddress.setApi(network.api());
        mongoSmartContractAddress.setNetwork(network);
        mongoSmartContractAddress.setAddress(smartContractAddress.getAddress());
        return mongoSmartContractAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BlockchainApi getApi() {
        return api;
    }

    public void setApi(BlockchainApi api) {
        this.api = api;
    }

    public BlockchainNetwork getNetwork() {
        return network;
    }

    public void setNetwork(BlockchainNetwork network) {
        this.network = network;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoSmartContractAddress{");
        sb.append("address='").append(address).append('\'');
        sb.append(", api=").append(api);
        sb.append(", network=").append(network);
        sb.append('}');
        return sb.toString();
    }

}
