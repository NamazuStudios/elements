package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

public interface AdvancedInventoryItemService {

    InventoryItem getInventoryItem(String itemNameOrId);

    Pagination<InventoryItem> getInventoryItems(int offset, int count, String userId);

    Pagination<InventoryItem> getInventoryItems(int offset, int count, String userId, String query);

    InventoryItem adjustInventoryItemQuantity(String userId, String itemNameOrId, int quantityDelta);

    InventoryItem createInventoryItem(String userId, String itemId, int quantity);

    void deleteInventoryItem(String inventoryItemId);


}
