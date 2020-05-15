package com.namazustudios.socialengine.model.inventory;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.goods.Item;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class CreateInventoryItem {

    @NotNull(groups = Insert.class)
    private User user;

    @NotNull
    @ApiModelProperty("The item to reference.")
    private Item item;

    @NotNull
    @ApiModelProperty("The quantity of the Item in inventory")
    @Min(value = 0, message = "Quantity may not be less than 0")
    private Integer quantity;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CreateInventoryItem)) return false;
        CreateInventoryItem that = (CreateInventoryItem) object;
        return Objects.equals(getItem(), that.getItem()) &&
                Objects.equals(getQuantity(), that.getQuantity());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getItem(), getQuantity());
    }

    @Override
    public String toString() {
        return "CreateInventoryItem{" +
                "item=" + item +
                ", quantity=" + quantity +
                '}';
    }

}
