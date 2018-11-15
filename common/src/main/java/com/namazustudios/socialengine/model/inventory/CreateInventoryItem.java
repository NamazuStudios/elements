package com.namazustudios.socialengine.model.inventory;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CreateInventoryItem {
    @NotNull
    @ApiModelProperty("The quantity of the Item in inventory")
    @Min(value = 0, message = "Quantity may not be less than 0")
    private Integer quantity;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryItem)) return false;

        CreateInventoryItem createInventoryItem = (CreateInventoryItem) o;

        return (getQuantity() != null ? !getQuantity().equals(createInventoryItem.getQuantity()) : createInventoryItem.getQuantity() != null);
    }

    @Override
    public int hashCode() {
        int result = getQuantity() != null ? getQuantity().hashCode() : 0;
        return result;
    }

    @Override
    public String toString() {
        return "CreateInventoryItem{" +
                "quantity='" + quantity + '\'' +
                '}';
    }
}
