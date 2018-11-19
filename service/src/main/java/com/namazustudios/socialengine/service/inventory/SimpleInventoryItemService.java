package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.goods.Item;

import java.util.Set;

public interface SimpleInventoryItemService {

    /**
     * Returns the {@link InventoryItem} associated with the specified {@Link Item}.
     *
     * @param itemNameOrId the value of {@link Item#getId()} or {@link Item#getName()}
     * @return the {@link InventoryItem} associated with specified item
     */
    InventoryItem getInventoryItem(String itemNameOrId);

    /**
     * Returns a list of {@link InventoryItem} objects.
     *
     * @param offset the offset
     * @param count the count
     * @return the list of {@link InventoryItem} instances
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count);

    /**
     * Returns a list of {@link InventoryItem} objects.
     *
     * @param offset the offset
     * @param count the count
     * @param query the search query
     * @return the list of {@link InventoryItem} instances
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count, String query);

    /**
     * Adjusts the quantity of the {@link InventoryItem} associated with the specified {@Link Item}.
     *
     * @param itemNameOrId the value of {@link Item#getId()} or {@link Item#getName()}
     * @param quantityDelta the amount by which to adjust the quantity of the {@Link InventoryItem}
     * @return the {@link InventoryItem} as it was written to the database
     */
    InventoryItem adjustInventoryItemQuantity(String itemNameOrId, Integer quantityDelta);

    /**
     * Creates a new {@link InventoryItem} for the specified {@link Item}.
     *
     * @param itemNameOrId the value of {@link Item#getId()} or {@link Item#getName()}
     * @param initialQuantity the initial quantity
     * @return the {@link InventoryItem} as it was written to the database
     */
    InventoryItem createInventoryItem(String itemNameOrId, Integer initialQuantity);

    /**
     * Deletes the single {@link InventoryItem} associated with {@link Item}.
     *
     * @param itemNameOrId the value of {@link Item#getId()} or {@link Item#getName()}
     */
    void deleteInventoryItem(String itemNameOrId);
}
