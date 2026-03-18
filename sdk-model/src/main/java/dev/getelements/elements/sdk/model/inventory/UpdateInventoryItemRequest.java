package dev.getelements.elements.sdk.model.inventory;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a request to update the quantity of an inventory item. */
@Schema
public class UpdateInventoryItemRequest {

    /** Creates a new instance. */
    public UpdateInventoryItemRequest() {}

    @Schema(description = "The quantity of the Item in inventory")
    @Min(value = 1, message = "Quantity may not be less than 0")
    private int quantity;

    /**
     * Returns the quantity of the item in inventory.
     *
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the item in inventory.
     *
     * @param quantity the quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateInventoryItemRequest that = (UpdateInventoryItemRequest) o;
        return getQuantity() == that.getQuantity();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuantity());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateInventoryItemRequest{");
        sb.append("quantity=").append(quantity);
        sb.append('}');
        return sb.toString();
    }

}
