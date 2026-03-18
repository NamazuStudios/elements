package dev.getelements.elements.sdk.model.inventory;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a request to adjust an inventory item quantity for a given user by a delta amount. */
@Schema
public class SimpleInventoryItemQuantityAdjustment {

    /** Creates a new instance. */
    public SimpleInventoryItemQuantityAdjustment() {}

    @NotNull
    @Schema(description = "The User whose inventory to modify.ß")
    private String userId;

    @NotNull
    @Schema(description = "The delta to be applied to the inventory item quantity (positive or negative)")
    private int quantityDelta;

    /**
     * Returns the ID of the user whose inventory to modify.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user whose inventory to modify.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the delta to be applied to the inventory item quantity (positive or negative).
     *
     * @return the quantity delta
     */
    public int getQuantityDelta() {
        return quantityDelta;
    }

    /**
     * Sets the delta to be applied to the inventory item quantity (positive or negative).
     *
     * @param quantityDelta the quantity delta
     */
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
