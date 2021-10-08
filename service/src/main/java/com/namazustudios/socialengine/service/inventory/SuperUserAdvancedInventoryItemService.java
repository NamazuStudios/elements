package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.service.AdvancedInventoryItemService;

public class SuperUserAdvancedInventoryItemService implements AdvancedInventoryItemService {

    @Override
    public InventoryItem getInventoryItem(String itemNameOrId) {
        return null;
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(int offset, int count, String userId) {
        return null;
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(int offset, int count, String userId, String query) {
        return null;
    }

    @Override
    public InventoryItem adjustInventoryItemQuantity(String userId, String itemNameOrId, int quantityDelta, int priority) {
        return null;
    }

    @Override
    public InventoryItem createInventoryItem(String userId, String itemId, int quantity, int priority) {
        return null;
    }

    @Override
    public void deleteInventoryItem(String inventoryItemId) {

    }

}
