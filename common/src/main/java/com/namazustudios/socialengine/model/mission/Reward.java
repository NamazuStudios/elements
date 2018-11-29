package com.namazustudios.socialengine.model.mission;

import com.namazustudios.socialengine.model.goods.Item;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Represents a mission step reward.
 *
 * Created by davidjbrooks on 11/23/2018.
 */
@ApiModel
public class Reward {

    @NotNull
    @ApiModelProperty("The Item that constitutes the reward")
    private Item item;

    @NotNull
    @Min(value = 0, message = "Quantity may not be less than 0")
    @ApiModelProperty("The quantity of the Item that is rewarded")
    private Integer quantity;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reward)) return false;

        Reward reward = (Reward) o;

        if (getItem() != null ? !getItem().equals(reward.getItem()) : reward.getItem() != null) return false;
        return (getQuantity() != null ? !getQuantity().equals(reward.getQuantity()) : reward.getQuantity() != null);
    }

    @Override
    public int hashCode() {
        int result = (getItem() != null ? getItem().hashCode() : 0);
        result = 31 * result + (getQuantity() != null ? getQuantity().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Reward{" +
                ", item='" + item + '\'' +
                ", quantity='" + quantity + '\'' +
                '}';
    }

}
