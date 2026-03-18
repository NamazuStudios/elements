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

    /** Creates a new instance. */
    public ProductBundleReward() {}

    @NotNull
    @Schema(description = "The id of the item to be rewarded.")
    private String itemId;

    @Schema(description = "The quantity of the item to be rewarded. Null for DISTINCT items; positive integer for FUNGIBLE items.")
    private Integer quantity;

    /**
     * Returns the ID of the item to be rewarded.
     *
     * @return the item ID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Sets the ID of the item to be rewarded.
     *
     * @param itemId the item ID
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /**
     * Returns the quantity of the item to be rewarded.
     *
     * @return the quantity
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the item to be rewarded.
     *
     * @param quantity the quantity
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ProductBundleReward reward = (ProductBundleReward) object;
        return Objects.equals(getQuantity(), reward.getQuantity()) && Objects.equals(getItemId(), reward.getItemId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItemId(), getQuantity());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProductBundleReward{");
        sb.append("itemId='").append(itemId).append('\'');
        sb.append(", quantity=").append(quantity);
        sb.append('}');
        return sb.toString();
    }
    
}

