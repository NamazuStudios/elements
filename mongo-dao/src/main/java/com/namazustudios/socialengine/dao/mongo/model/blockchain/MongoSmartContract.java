package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity(value = "wallet")
@Indexes({
        @Index(fields = @Field("api")),
        @Index(fields = @Field("networks"))
})
public class MongoSmartContract {

    @Id
    private ObjectId objectId;

    @Property
    private String displayName;

    @Property
    private Set<BlockchainNetwork> networks;

    @Property
    private Map<BlockchainNetwork, MongoSmartContractAddress> addresses;

    @Property
    private BlockchainApi api;

    @Reference
    private MongoWallet wallet;

    @Property
    private Map<String, Object> metadata;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<BlockchainNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(Set<BlockchainNetwork> networks) {
        this.networks = networks;
    }

    public Map<BlockchainNetwork, MongoSmartContractAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<BlockchainNetwork, MongoSmartContractAddress> addresses) {
        this.addresses = addresses;
    }

    public BlockchainApi getApi() {
        return api;
    }

    public void setApi(BlockchainApi api) {
        this.api = api;
    }

    public MongoWallet getWallet() {
        return wallet;
    }

    public void setWallet(MongoWallet wallet) {
        this.wallet = wallet;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoSmartContract that = (MongoSmartContract) o;
        return Objects.equals(objectId, that.objectId) && Objects.equals(displayName, that.displayName) && Objects.equals(networks, that.networks) && Objects.equals(addresses, that.addresses) && api == that.api && Objects.equals(wallet, that.wallet) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, displayName, networks, addresses, api, wallet, metadata);
    }

    @Override
    public String toString() {
        return "MongoSmartContract{" +
                "objectId=" + objectId +
                ", displayName='" + displayName + '\'' +
                ", networks=" + networks +
                ", addresses=" + addresses +
                ", api=" + api +
                ", wallet=" + wallet +
                ", metadata=" + metadata +
                '}';
    }

}
