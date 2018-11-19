package com.namazustudios.socialengine.model.inventory;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

public class InventoryItemQuantityAdjustment {
    @NotNull
    @ApiModelProperty("The delta to be applied to the inventory item quantity (positive or negative)")
    private Integer quantityDelta;

    public Integer getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(Integer quantityDelta) { this.quantityDelta = quantityDelta; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryItem)) return false;

        InventoryItemQuantityAdjustment inventoryItemQuantityAdjustment = (InventoryItemQuantityAdjustment) o;

        return (getQuantityDelta() != null ? !getQuantityDelta().equals(inventoryItemQuantityAdjustment.getQuantityDelta()) : inventoryItemQuantityAdjustment.getQuantityDelta() != null);
    }

    @Override
    public int hashCode() {
        int result = getQuantityDelta() != null ? getQuantityDelta().hashCode() : 0;

        return result;
    }

    @Override
    public String toString() {
        return "InventoryItemQuantityAdjustment{" +
                ", quantityDelta='" + quantityDelta + '\'' +
                '}';
    }
}
