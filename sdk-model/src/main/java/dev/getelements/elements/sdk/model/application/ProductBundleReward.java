package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * Represents a single reward to be issued when a {@link ProductBundle} is purchased, i.e. the item id and the quantity
 * of that item to be rewarded to the user.
 *
 */
@Schema
public class ProductBundleReward implements Serializable {

    @NotNull
    @Schema(description = "The id of the item to be rewarded.")
    private String itemId;

    @NotNull
    @Schema(description = "The quantity of the item to be rewarded.")
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

