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

    @NotNull
    @Schema(description = "The Item that constitutes the reward")
    private Item item;

    @NotNull
    @Min(value = 0, message = "Quantity may not be less than 0")
    @Schema(description = "The quantity of the Item that is rewarded")
    private Integer quantity;

    private Map<String, Object> metadata;

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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

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
