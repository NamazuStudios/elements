package com.namazustudios.socialengine.model.inventory;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class SimpleInventoryItemQuantityAdjustment {

    @NotNull
    @ApiModelProperty("The User whose inventory to modify.ß")
    private String userId;

    @NotNull
    @ApiModelProperty("The delta to be applied to the inventory item quantity (positive or negative)")
    private int quantityDelta;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(int quantityDelta) {
        this.quantityDelta = quantityDelta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleInventoryItemQuantityAdjustment that = (SimpleInventoryItemQuantityAdjustment) o;
        return getQuantityDelta() == that.getQuantityDelta() && Objects.equals(getUserId(), that.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getQuantityDelta());
    }

}
