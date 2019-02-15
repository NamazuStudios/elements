package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.Map;
import java.util.Objects;

/**
 * Mongo DTO for a mission step reward.
 *
 * Created by davidjbrooks on 11/27/2018.
 */

@Embedded
public class MongoReward {
    @Reference
    private MongoItem item;

    @Property
    private int quantity;

    @Property
    private Map<String, Object> metadata;

    public MongoItem getItem() {
        return item;
    }

    public void setItem(MongoItem item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoReward)) return false;
        MongoReward that = (MongoReward) object;
        return getQuantity() == that.getQuantity() &&
                Objects.equals(getItem(), that.getItem()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItem(), getQuantity(), getMetadata());
    }

    @Override
    public String toString() {
        return "MongoReward{" +
                "item=" + item +
                ", quantity=" + quantity +
                ", metadata=" + metadata +
                '}';
    }

}
