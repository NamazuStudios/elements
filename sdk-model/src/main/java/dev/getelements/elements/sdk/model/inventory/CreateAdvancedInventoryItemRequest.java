package dev.getelements.elements.sdk.model.inventory;



import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Request model for creating an advanced inventory item for a specific user. */
@Schema
public class CreateAdvancedInventoryItemRequest {

    /** Creates a new instance. */
    public CreateAdvancedInventoryItemRequest() {}

    @NotNull
    @Schema(description = "The User ID")
    private String userId;

    @NotNull
    @Schema(description = "The item to reference.")
    private String itemId;

    @Schema(description = "The quantity of the Item in inventory")
    @Min(value = 0, message = "Quantity may not be less than 0.")
    private int quantity;

    @Schema(description = "The priority slot for the item.")
    @Min(value = 0, message = "Priority must be greater than 0.")
    private int priority;

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
     * Returns the quantity of the item.
     *
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
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
     * Returns the priority slot for the item.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority slot for the item.
     *
     * @param priority the priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateAdvancedInventoryItemRequest that = (CreateAdvancedInventoryItemRequest) o;
        return getQuantity() == that.getQuantity() && getPriority() == that.getPriority() && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getItemId(), that.getItemId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getItemId(), getQuantity(), getPriority());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateAdvancedInventoryItemRequest{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", itemId='").append(itemId).append('\'');
        sb.append(", quantity=").append(quantity);
        sb.append(", priority=").append(priority);
        sb.append('}');
        return sb.toString();
    }

}
