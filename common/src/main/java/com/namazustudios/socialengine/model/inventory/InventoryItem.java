package com.namazustudios.socialengine.model.inventory;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.ValidationGroups;
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
    @ApiModelProperty("The User associated with this InventoryItem.")
    private User user;

    @NotNull
    @ApiModelProperty("The Item in inventory")
    private Item item;

    @NotNull
    @ApiModelProperty("The quantity of the Item in inventory")
    @Min(value = 0, message = "Quantity may not be less than 0")
    private Integer quantity;

    @NotNull
    @ApiModelProperty("The priority of this Item grouping in inventory (for stacked/packaged inventory support)")
    private Integer priority;

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) { this.priority = priority; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof InventoryItem)) return false;
        InventoryItem that = (InventoryItem) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getItem(), that.getItem()) &&
                Objects.equals(getQuantity(), that.getQuantity()) &&
                Objects.equals(getPriority(), that.getPriority());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser(), getItem(), getQuantity(), getPriority());
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", item='" + item + '\'' +
                ", quantity='" + quantity + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }

}
