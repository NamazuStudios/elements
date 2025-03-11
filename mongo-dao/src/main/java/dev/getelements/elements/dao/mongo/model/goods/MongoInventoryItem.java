package dev.getelements.elements.dao.mongo.model.goods;

import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.morphia.annotations.*;

import java.util.Objects;
import java.util.Set;

@Entity(value = "inventory_items", useDiscriminator = false)
public class MongoInventoryItem {

    @Id
    private MongoInventoryItemId objectId;

    @Indexed
    @Property
    private String version;

    @Indexed
    @Reference
    private MongoItem item;

    @Indexed
    @Reference
    private MongoUser user;

    @Property
    private int quantity;

    @Property
    private Set<String> rewardIssuanceUuids;

    public MongoInventoryItemId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoInventoryItemId objectId) {
        this.objectId = objectId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public MongoItem getItem() { return item; }

    public void setItem(MongoItem item) {
        this.item = item;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Set<String> getRewardIssuanceUuids() {
        return rewardIssuanceUuids;
    }

    public void setRewardIssuanceUuids(Set<String> rewardIssuanceUuids) {
        this.rewardIssuanceUuids = rewardIssuanceUuids;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoInventoryItem)) return false;
        MongoInventoryItem that = (MongoInventoryItem) object;
        return getQuantity() == that.getQuantity() &&
                Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getVersion(), that.getVersion()) &&
                Objects.equals(getItem(), that.getItem()) &&
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getRewardIssuanceUuids(), that.getRewardIssuanceUuids());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getVersion(), getItem(), getUser(), getQuantity(), getRewardIssuanceUuids());
    }

    @Override
    public String toString() {
        return "MongoInventoryItem{" +
                "objectId=" + objectId +
                ", version='" + version + '\'' +
                ", item=" + item +
                ", user=" + user +
                ", quantity=" + quantity +
                ", rewardIssuanceUuids=" + rewardIssuanceUuids +
                '}';
    }
}
