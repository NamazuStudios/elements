package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.socialengine.model.goods.Item;

/**
 * Mongo DTO for a mission step reward.
 *
 * This is NOT an entity, and is therefore not directly searchable
 *
 * As a purely embedded object, we could have leveraged the domain model - however, we may change our mind and create
 * a collection for this object and/or implement mongo-specific logic here
 *
 * Created by davidjbrooks on 11/27/2018.
 */
public class MongoReward {

    private Item item;

    private Integer quantity;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
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
