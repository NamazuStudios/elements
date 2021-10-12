package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

/**
 * Allows for modification of the inventory using the advanced API.
 */
public interface AdvancedInventoryItemService {

    /**
     * Gets an inventory item with the supplied item name or ID.
     *
     * @param itemNameOrId the item name or id
     * @return the inventory item.
     */
    InventoryItem getInventoryItem(String itemNameOrId);

    /**
     * Gets inventory items with the supplied count and user id.
     *
     * @param offset
     * @param count
     * @param userId
     * @return
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count, String userId);

    Pagination<InventoryItem> getInventoryItems(int offset, int count, String userId, String query);

    InventoryItem adjustInventoryItemQuantity(String userId, String itemNameOrId, int quantityDelta, int priority);

    InventoryItem createInventoryItem(String userId, String itemId, int quantity, int priority);

    void deleteInventoryItem(String inventoryItemId);


}
