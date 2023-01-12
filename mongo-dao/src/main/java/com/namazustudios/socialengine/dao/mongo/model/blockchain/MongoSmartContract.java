package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Entity(value = "wallet")
@Indexes({
        @Index(fields = @Field("apis")),
        @Index(fields = @Field("name")),
        @Index(fields = @Field("networks"))
})
public class MongoSmartContract {

    @Id
    private ObjectId objectId;

    @Property
    private String name;

    @Property
    private String displayName;

    @Property
    private List<BlockchainApi> apis;

    @Property
    private List<BlockchainNetwork> networks;

    @Property
    private Map<BlockchainNetwork, MongoSmartContractAddress> addresses;

    @Property
    private MongoVault vault;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<BlockchainApi> getApis() {
        return apis;
    }

    public void setApis(List<BlockchainApi> apis) {
        this.apis = apis;
    }

    public List<BlockchainNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<BlockchainNetwork> networks) {
        this.networks = networks;
    }

    public Map<BlockchainNetwork, MongoSmartContractAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<BlockchainNetwork, MongoSmartContractAddress> addresses) {
        this.addresses = addresses;
    }

    public MongoVault getVault() {
        return vault;
    }

    public void setVault(MongoVault vault) {
        this.vault = vault;
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
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoSmartContract{");
        sb.append("objectId=").append(objectId);
        sb.append(", name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", apis=").append(apis);
        sb.append(", networks=").append(networks);
        sb.append(", addresses=").append(addresses);
        sb.append(", vault=").append(vault);
        sb.append(", wallet=").append(wallet);
        sb.append(", metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }

}
