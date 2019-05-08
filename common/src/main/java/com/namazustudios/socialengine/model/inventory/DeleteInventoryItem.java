package com.namazustudios.socialengine.model.inventory;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.goods.Item;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class DeleteInventoryItem {

    @NotNull(groups = Insert.class)
    private User user;

    @NotNull
    @ApiModelProperty("The item to reference.")
    private Item item;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteInventoryItem that = (DeleteInventoryItem) o;
        return Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getItem(), that.getItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getItem());
    }

    @Override
    public String toString() {
        return "DeleteInventoryItem{" +
                "user=" + user +
                ", item=" + item +
                '}';
    }
}
