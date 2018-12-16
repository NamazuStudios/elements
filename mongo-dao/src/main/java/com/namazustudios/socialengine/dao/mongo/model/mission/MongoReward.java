package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.Objects;

/**
 * Mongo DTO for a mission step reward.
 *
 * This is NOT an entity, and is therefore not directly searchable

 * Created by davidjbrooks on 11/27/2018.
 */

@Embedded
public class MongoReward {

    @Reference
    private MongoItem item;

    @Property
    private int quantity;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoReward)) return false;
        MongoReward that = (MongoReward) object;
        return getQuantity() == that.getQuantity() && Objects.equals(getItem(), that.getItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItem(), getQuantity());
    }

    @Override
    public String toString() {
        return "MongoReward{" +
                ", item='" + item + '\'' +
                ", quantity='" + quantity + '\'' +
                '}';
    }

}
