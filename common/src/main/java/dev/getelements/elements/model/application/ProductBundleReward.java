package dev.getelements.elements.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * Represents a single reward to be issued when a {@link ProductBundle} is purchased, i.e. the item id and the quantity
 * of that item to be rewarded to the user.
 *
 */
@ApiModel
public class ProductBundleReward implements Serializable {
    @NotNull
    @ApiModelProperty("The id of the item to be rewarded.")
    private String itemId;

    @NotNull
    @ApiModelProperty("The quantity of the item to be rewarded.")
    private Integer quantity;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductBundleReward that = (ProductBundleReward) o;
        return Objects.equals(getItemId(), that.getItemId()) &&
                Objects.equals(getQuantity(), that.getQuantity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItemId(), getQuantity());
    }

    @Override
    public String toString() {
        return "ProductBundleReward{" +
                "itemId='" + itemId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}

