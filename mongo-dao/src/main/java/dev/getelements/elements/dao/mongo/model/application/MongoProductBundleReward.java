package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.util.Objects;

@Embedded
public class MongoProductBundleReward {

    @Property
    private ObjectId itemId;

    @Property
    private int quantity;

    public ObjectId getItemId() {
        return itemId;
    }

    public void setItemId(ObjectId itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoProductBundleReward that = (MongoProductBundleReward) o;
        return getQuantity() == that.getQuantity() &&
                Objects.equals(getItemId(), that.getItemId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItemId(), getQuantity());
    }
}
