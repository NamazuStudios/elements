package com.namazustudios.socialengine.model.inventory;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class AdvancedInventoryItemQuantityAdjustment {

    @NotNull
    @ApiModelProperty("The User whose inventory to modify.ß")
    private String userId;

    @NotNull
    @ApiModelProperty("The delta to be applied to the inventory item quantity (positive or negative)")
    private int quantityDelta;

    @ApiModelProperty("The priority slot for the item.")
    @Min(value = 0, message = "Priority must be greater than 0.")
    private int priority;

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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdvancedInventoryItemQuantityAdjustment that = (AdvancedInventoryItemQuantityAdjustment) o;
        return getQuantityDelta() == that.getQuantityDelta() && getPriority() == that.getPriority() && Objects.equals(getUserId(), that.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getQuantityDelta(), getPriority());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AdvancedInventoryItemQuantityAdjustment{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", quantityDelta=").append(quantityDelta);
        sb.append(", priority=").append(priority);
        sb.append('}');
        return sb.toString();
    }

}
