package com.namazustudios.socialengine.model.inventory;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.goods.Item;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Represents an inventory item.
 *
 * Created by davidjbrooks on 11/11/2018.
 */
@ApiModel
public class InventoryItem implements Serializable {

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

    public void setPriority(Integer priority) { this.quantity = quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryItem)) return false;

        InventoryItem inventoryItem = (InventoryItem) o;

        if (getId() != null ? !getId().equals(inventoryItem.getId()) : inventoryItem.getId() != null) return false;
        if (getUser() != null ? !getUser().equals(inventoryItem.getUser()) : inventoryItem.getUser() != null) return false;
        if (getItem() != null ? !getItem().equals(inventoryItem.getItem()) : inventoryItem.getItem() != null) return false;
        if (getQuantity() != null ? !getQuantity().equals(inventoryItem.getQuantity()) : inventoryItem.getQuantity() != null) return false;
        return (getPriority() != null ? !getPriority().equals(inventoryItem.getPriority()) : inventoryItem.getPriority() != null);
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getItem() != null ? getItem().hashCode() : 0);
        result = 31 * result + (getQuantity() != null ? getQuantity().hashCode() : 0);
        result = 31 * result + (getPriority() != null ? getPriority().hashCode() : 0);
        return result;
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
