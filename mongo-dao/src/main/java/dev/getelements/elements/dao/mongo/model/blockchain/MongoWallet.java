package dev.getelements.elements.dao.mongo.model.blockchain;

import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.model.blockchain.BlockchainApi;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;

@Entity(value = "wallet")
@Indexes({
        @Index(fields = @Field("api")),
        @Index(fields = @Field("user")),
        @Index(fields = @Field("networks")),
        @Index(fields = {
                @Field("api"),
                @Field("networks")
        })
})
public class MongoWallet {

    @Id
    private ObjectId objectId;

    @Reference
    private MongoUser user;

    @Reference
    private MongoVault vault;

    @Property
    private String displayName;

    @Property
    private BlockchainApi api;

    @Property
    private List<BlockchainNetwork> networks;

    @Property
    private int preferredAccount;

    @Property
    private List<MongoWalletAccount> accounts;

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

    public MongoVault getVault() {
        return vault;
    }

    public void setVault(MongoVault vault) {
        this.vault = vault;
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

    public int getPreferredAccount() {
        return preferredAccount;
    }

    public void setPreferredAccount(int preferredAccount) {
        this.preferredAccount = preferredAccount;
    }

    public List<MongoWalletAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<MongoWalletAccount> accounts) {
        this.accounts = accounts;
    }

}
