package dev.getelements.elements.sdk.model.inventory;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.ValidationGroups.Create;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.goods.Item;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an inventory item.
 *
 * Created by davidjbrooks on 11/11/2018.
 */
@Schema
public class InventoryItem implements Serializable {

    @Null(groups = {Create.class, Insert.class})
    @NotNull(groups = Update.class)
    @Schema(description = "The unique ID of the inventory item itself.")
    private String id;

    @NotNull
    @Null(groups = Update.class)
    @Schema(description = "The User associated with this InventoryItem.")
    private User user;

    @NotNull
    @Null(groups = Update.class)
    @Schema(description = "The Item in inventory")
    private Item item;

    @NotNull
    @Schema(description = "The quantity of the Item in inventory")
    @Min(value = 0, message = "Quantity may not be less than 0")
    private int quantity;

    @NotNull
    @Schema(description = "The priority of this Item grouping in inventory (for stacked/packaged inventory support)")
    private int priority;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
        InventoryItem that = (InventoryItem) o;
        return getQuantity() == that.getQuantity() && getPriority() == that.getPriority() && Objects.equals(getId(), that.getId()) && Objects.equals(getUser(), that.getUser()) && Objects.equals(getItem(), that.getItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser(), getItem(), getQuantity(), getPriority());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InventoryItem{");
        sb.append("id='").append(id).append('\'');
        sb.append(", user=").append(user);
        sb.append(", item=").append(item);
        sb.append(", quantity=").append(quantity);
        sb.append(", priority=").append(priority);
        sb.append('}');
        return sb.toString();
    }

}
