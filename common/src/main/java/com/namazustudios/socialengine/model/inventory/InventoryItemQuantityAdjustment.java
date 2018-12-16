package com.namazustudios.socialengine.model.inventory;

import com.namazustudios.socialengine.model.User;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class InventoryItemQuantityAdjustment {

    @NotNull
    @ApiModelProperty("The User whose inventory to modify.ß")
    private User user;

    @NotNull
    @ApiModelProperty("The delta to be applied to the inventory item quantity (positive or negative)")
    private Integer quantityDelta;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(Integer quantityDelta) { this.quantityDelta = quantityDelta; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof InventoryItemQuantityAdjustment)) return false;
        InventoryItemQuantityAdjustment that = (InventoryItemQuantityAdjustment) object;
        return Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getQuantityDelta(), that.getQuantityDelta());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getUser(), getQuantityDelta());
    }

    @Override
    public String toString() {
        return "InventoryItemQuantityAdjustment{" +
                "user=" + user +
                ", quantityDelta=" + quantityDelta +
                '}';
    }

}
