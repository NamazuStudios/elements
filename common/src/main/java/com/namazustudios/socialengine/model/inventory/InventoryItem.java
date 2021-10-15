package com.namazustudios.socialengine.model.inventory;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.goods.Item;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an inventory item.
 *
 * Created by davidjbrooks on 11/11/2018.
 */
@ApiModel
public class InventoryItem implements Serializable {

    @Null(groups = {Create.class, Insert.class})
    @NotNull(groups = Update.class)
    @ApiModelProperty("The unique ID of the inventory item itself.")
    private String id;

    @NotNull
    @Null(groups = Update.class)
    @ApiModelProperty("The User associated with this InventoryItem.")
    private User user;

    @NotNull
    @Null(groups = Update.class)
    @ApiModelProperty("The Item in inventory")
    private Item item;

    @NotNull
    @ApiModelProperty("The quantity of the Item in inventory")
    @Min(value = 0, message = "Quantity may not be less than 0")
    private int quantity;

    @NotNull
    @ApiModelProperty("The priority of this Item grouping in inventory (for stacked/packaged inventory support)")
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
