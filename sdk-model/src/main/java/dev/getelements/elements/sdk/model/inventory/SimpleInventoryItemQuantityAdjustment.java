package dev.getelements.elements.sdk.model.inventory;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Schema
public class SimpleInventoryItemQuantityAdjustment {

    @NotNull
    @Schema(description = "The User whose inventory to modify.ß")
    private String userId;

    @NotNull
    @Schema(description = "The delta to be applied to the inventory item quantity (positive or negative)")
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
