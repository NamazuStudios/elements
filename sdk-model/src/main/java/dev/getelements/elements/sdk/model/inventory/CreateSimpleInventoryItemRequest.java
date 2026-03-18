package dev.getelements.elements.sdk.model.inventory;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Request model for creating a simple inventory item for a specific user.
 */
@Schema
public class CreateSimpleInventoryItemRequest {

    /** Creates a new instance. */
    public CreateSimpleInventoryItemRequest() {}

    @NotNull
    @Schema(description = "The User ID")
    private String userId;

    @NotNull
    @Schema(description = "The item to reference.")
    private String itemId;

    @Schema(description = "The quantity of the Item in inventory")
    @Min(value = 1, message = "Quantity may not be less than 0")
    private int quantity;

    /**
     * Returns the user ID whose inventory will be modified.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID whose inventory will be modified.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the item ID to add to inventory.
     *
     * @return the item ID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Sets the item ID to add to inventory.
     *
     * @param itemId the item ID
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /**
     * Sets the quantity of the item.
     *
     * @param quantity the quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Returns the quantity of the item.
     *
     * @return the quantity
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the item.
     *
     * @param quantity the quantity
     */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateSimpleInventoryItemRequest that = (CreateSimpleInventoryItemRequest) o;
        return getQuantity() == that.getQuantity() && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getItemId(), that.getItemId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getItemId(), getQuantity());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateSimpleInventoryItemRequest{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", itemId='").append(itemId).append('\'');
        sb.append(", quantity=").append(quantity);
        sb.append('}');
        return sb.toString();
    }

}
