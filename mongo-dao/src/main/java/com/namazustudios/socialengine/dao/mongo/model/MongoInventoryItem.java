package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


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
    private ObjectId objectId;

    @Reference
    @Indexed()
    private MongoItem item;

    @Reference
    @Indexed()
    private MongoUser user;

    @Property
    private Integer quantity;

    @Property
    @Indexed()
    private Integer priority;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MongoInventoryItem mongoInventoryItem = (MongoInventoryItem) o;

        if (getObjectId() != null ? !getObjectId().equals(mongoInventoryItem.getObjectId()) : mongoInventoryItem.getObjectId() != null) return false;
        if (getUser() != null ? !getUser().equals(mongoInventoryItem.getUser()) : mongoInventoryItem.getUser() != null) return false;
        if (getItem() != null ? !getItem().equals(mongoInventoryItem.getItem()) : mongoInventoryItem.getItem() != null) return false;
        if (getQuantity() != null ? !getItem().equals(mongoInventoryItem.getQuantity()) : mongoInventoryItem.getQuantity() != null) return false;
        return (getPriority() != null ? !getPriority().equals(mongoInventoryItem.getPriority()) : mongoInventoryItem.getPriority() != null);
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getItem() != null ? getItem().hashCode() : 0);
        result = 31 * result + (getQuantity() != null ? getQuantity().hashCode() : 0);
        result = 31 * result + (getPriority() != null ? getPriority().hashCode() : 0);
        return result;
    }
}
