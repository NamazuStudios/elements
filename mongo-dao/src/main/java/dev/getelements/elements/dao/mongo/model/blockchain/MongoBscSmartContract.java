package dev.getelements.elements.dao.mongo.model.blockchain;

import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

import java.util.Map;
@Entity(value = "contract", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field(value = "displayName", type = IndexType.TEXT))
})
public class MongoBscSmartContract {

    @Id
    private ObjectId objectId;

    @Property
    private String displayName;

    @Property
    private String scriptHash;

    @Property
    private String blockchain;

    @Property
    private String walletId;

    @Property
    private String accountAddress;

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

    public String getScriptHash() {
        return scriptHash;
    }

    public void setScriptHash(String scriptHash) {
        this.scriptHash = scriptHash;
    }

    public String getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(String blockchain) {
        this.blockchain = blockchain;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getAccountAddress() {
        return accountAddress;
    }

    public void setAccountAddress(String accountAddress) {
        this.accountAddress = accountAddress;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
