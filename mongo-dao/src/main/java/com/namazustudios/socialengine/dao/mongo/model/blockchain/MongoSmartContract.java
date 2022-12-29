package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Entity(value = "wallet")
@Indexes({
        @Index(fields = @Field("api")),
        @Index(fields = @Field("networks")),
        @Index(fields = {
                @Field("api"),
                @Field("networks")
        } )
})
public class MongoSmartContract {

    @Id
    private ObjectId objectId;

    @Property
    private String displayName;

    @Property
    private String address;

    @Property
    private BlockchainApi api;

    @Property
    private List<BlockchainNetwork> networks;

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

    public List<BlockchainNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<BlockchainNetwork> networks) {
        this.networks = networks;
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

}
