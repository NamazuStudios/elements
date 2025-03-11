package dev.getelements.elements.sdk.model.index;

import dev.getelements.elements.sdk.model.inventory.DistinctInventoryItem;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Indicates a specific type of indexable type.
 */
@Schema
public enum IndexableType {

    /**
     * Indexes for {@link DistinctInventoryItem}
     */
    DISTINCT_INVENTORY_ITEM

}
