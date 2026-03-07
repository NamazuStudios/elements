package dev.getelements.elements.dao.mongo.model.iap;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

@Embedded
public class MongoIapSkuReward {

    @Property
    private String itemId;

    @Property
    private Integer quantity;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
