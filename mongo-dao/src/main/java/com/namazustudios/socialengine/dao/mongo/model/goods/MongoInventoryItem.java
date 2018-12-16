package com.namazustudios.socialengine.dao.mongo.model.goods;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoPendingReward;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.*;


@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class))
@SearchableDocument(fields = {
        @SearchableField(name = "itemName",         path = "/item/name"),
        @SearchableField(name = "itemDisplayName",  path = "/item/displayName"),
        @SearchableField(name = "itemDescription",  path = "/item/description"),
        @SearchableField(name = "itemTags",         path = "/item/tags"),
        @SearchableField(name = "userName",         path = "/user/name"),
        @SearchableField(name = "userEmail",        path = "/user/email")
})
@Entity(value = "inventoryitems", noClassnameStored = true)
public class MongoInventoryItem {

    @Id
    private MongoInventoryItemId objectId;

    @Reference
    @Indexed
    private MongoItem item;

    @Reference
    @Indexed
    private MongoUser user;

    @Property
    private Integer quantity;

    @Property
    @Indexed
    private Integer priority;

    @Reference
    private List<MongoPendingReward> pendingRewards;

    public MongoInventoryItemId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoInventoryItemId objectId) {
        this.objectId = objectId;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<MongoPendingReward> getPendingRewards() {
        return pendingRewards;
    }

    public void setPendingRewards(List<MongoPendingReward> pendingRewards) {
        this.pendingRewards = pendingRewards;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoInventoryItem)) return false;
        MongoInventoryItem that = (MongoInventoryItem) object;
        return Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getItem(), that.getItem()) &&
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getQuantity(), that.getQuantity()) &&
                Objects.equals(getPriority(), that.getPriority()) &&
                Objects.equals(getPendingRewards(), that.getPendingRewards());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getItem(), getUser(), getQuantity(), getPriority(), getPendingRewards());
    }

    @Override
    public String toString() {
        return "MongoInventoryItem{" +
                "objectId=" + objectId +
                ", item=" + item +
                ", user=" + user +
                ", quantity=" + quantity +
                ", priority=" + priority +
                ", pendingRewards=" + pendingRewards +
                '}';
    }

}
