package dev.getelements.elements.model.inventory;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 *
 */
@ApiModel
public class CreateSimpleInventoryItemRequest {

    @NotNull
    @ApiModelProperty("The User ID")
    private String userId;

    @NotNull
    @ApiModelProperty("The item to reference.")
    private String itemId;

    @ApiModelProperty("The quantity of the Item in inventory")
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
