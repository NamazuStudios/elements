package dev.getelements.elements.service.inventory;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.inventory.InventoryItem;

public interface PublicInventoryItemService {

    /**
     * Returns a list of {@link InventoryItem} objects.
     *
     * @param offset the offset
     * @param count the count
     * @return the list of {@link InventoryItem} instances
     */
    Pagination<InventoryItem> getPublicInventoryItems(int offset, int count);
}
