package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

public class SuperUserSimpleInventoryItemService implements SimpleInventoryItemService {
    @Override
    public InventoryItem getInventoryItem(String itemNameOrId) {
        return null;
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(int offset, int count) {
        return null;
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(int offset, int count, String query) {
        return null;
    }

    @Override
    public InventoryItem adjustInventoryItemQuantity(String itemNameOrId, Integer quantityDelta) {
        return null;
    }

    @Override
    public InventoryItem createInventoryItem(String itemNameOrId, Integer initialQuantity) {
        return null;
    }

    @Override
    public void deleteInventoryItem(String itemNameOrId) {

    }
}
