package dev.getelements.elements.dao.mongo.model.blockchain;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Entity(value = "smart_contract")
@Indexes({
        @Index(fields = @Field("name"), options = @IndexOptions(unique = true)),
        @Index(fields = @Field("addresses.api")),
        @Index(fields = @Field("addresses.network"))
})
public class MongoSmartContract {

    @Id
    private ObjectId objectId;

    @Property
    private String name;

    @Property
    private String displayName;

    @Property
    private List<MongoSmartContractAddress> addresses;

    @Property
    private MongoVault vault;

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

    public List<MongoSmartContractAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<MongoSmartContractAddress> addresses) {
        this.addresses = addresses;
    }

    public MongoVault getVault() {
        return vault;
    }

    public void setVault(MongoVault vault) {
        this.vault = vault;
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
        sb.append(", addresses=").append(addresses);
        sb.append(", vault=").append(vault);
        sb.append(", metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }

}
