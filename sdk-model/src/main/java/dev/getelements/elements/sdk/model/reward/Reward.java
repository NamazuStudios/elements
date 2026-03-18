package dev.getelements.elements.sdk.model.reward;

import dev.getelements.elements.sdk.model.goods.Item;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a mission step reward.
 *
 * Created by davidjbrooks on 11/23/2018.
 */
@Schema
public class Reward implements Serializable {

    /** Creates a new instance. */
    public Reward() {}

    @NotNull
    @Schema(description = "The Item that constitutes the reward")
    private Item item;

    @NotNull
    @Min(value = 0, message = "Quantity may not be less than 0")
    @Schema(description = "The quantity of the Item that is rewarded")
    private Integer quantity;

    private Map<String, Object> metadata;

    /**
     * Returns the item that constitutes the reward.
     *
     * @return the item
     */
    public Item getItem() {
        return item;
    }

    /**
     * Sets the item that constitutes the reward.
     *
     * @param item the item
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * Returns the quantity of the item that is rewarded.
     *
     * @return the quantity
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the item that is rewarded.
     *
     * @param quantity the quantity
     */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    /**
     * Returns the metadata for this reward.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata for this reward.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Adds a single metadata entry to this reward, initializing the metadata map if necessary.
     *
     * @param name the metadata key
     * @param value the metadata value
     */
    public void addMetadata(final String name, final Object value) {

        if (getMetadata() == null) {
            setMetadata(new HashMap<>());
        }

        getMetadata().put(name, value);

    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Reward)) return false;
        Reward reward = (Reward) object;
        return Objects.equals(getItem(), reward.getItem()) &&
                Objects.equals(getQuantity(), reward.getQuantity()) &&
                Objects.equals(getMetadata(), reward.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItem(), getQuantity(), getMetadata());
    }

    @Override
    public String toString() {
        return "Reward{" +
                "item=" + item +
                ", quantity=" + quantity +
                ", metadata=" + metadata +
                '}';
    }

}
