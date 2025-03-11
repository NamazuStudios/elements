package dev.getelements.elements.sdk.model.inventory;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 *
 */
@Schema
public class CreateSimpleInventoryItemRequest {

    @NotNull
    @Schema(description = "The User ID")
    private String userId;

    @NotNull
    @Schema(description = "The item to reference.")
    private String itemId;

    @Schema(description = "The quantity of the Item in inventory")
    @Min(value = 1, message = "Quantity may not be less than 0")
    private int quantity;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

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
