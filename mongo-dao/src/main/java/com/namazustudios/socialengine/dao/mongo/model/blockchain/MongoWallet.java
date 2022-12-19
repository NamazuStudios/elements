package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Entity(value = "wallet")
@Indexes({
        @Index(fields = @Field("api")),
        @Index(fields = @Field("user")),
        @Index(fields = @Field("networks")),
        @Index(fields = {
                @Field("api"),
                @Field("networks")
        } )
})
public class MongoWallet {

    @Id
    private ObjectId objectId;

    @Reference
    private MongoUser user;

    @Property
    private String displayName;

    @Property
    private BlockchainApi api;

    @Property
    private List<BlockchainNetwork> networks;

    @Property
    private Map<String, Object> encryption;

    @Property
    private int defaultIdentity;

    @Property
    private List<MongoWalletIdentityPair> identities;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Map<String, Object> getEncryption() {
        return encryption;
    }

    public void setEncryption(Map<String, Object> encryption) {
        this.encryption = encryption;
    }

    public int getDefaultIdentity() {
        return defaultIdentity;
    }

    public void setDefaultIdentity(int defaultIdentity) {
        this.defaultIdentity = defaultIdentity;
    }

    public List<MongoWalletIdentityPair> getIdentities() {
        return identities;
    }

    public void setIdentities(List<MongoWalletIdentityPair> identities) {
        this.identities = identities;
    }

}
