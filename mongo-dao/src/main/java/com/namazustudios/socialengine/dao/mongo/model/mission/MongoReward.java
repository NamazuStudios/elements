package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.socialengine.dao.mongo.model.MongoItem;
import com.namazustudios.socialengine.model.goods.Item;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

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
    private Integer quantity;

    public MongoItem getItem() {
        return item;
    }

    public void setItem(MongoItem item) {
        this.item = item;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoReward)) return false;

        MongoReward mongoReward = (MongoReward) o;

        if (getItem() != null ? !getItem().equals(mongoReward.getItem()) : mongoReward.getItem() != null) return false;
        return (getQuantity() != null ? !getQuantity().equals(mongoReward.getQuantity()) : mongoReward.getQuantity() != null);
    }

    @Override
    public int hashCode() {
        int result = (getItem() != null ? getItem().hashCode() : 0);
        result = 31 * result + (getQuantity() != null ? getQuantity().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MongoReward{" +
                ", item='" + item + '\'' +
                ", quantity='" + quantity + '\'' +
                '}';
    }

}
