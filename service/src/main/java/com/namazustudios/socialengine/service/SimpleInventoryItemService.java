package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

import java.util.Set;

public interface SimpleInventoryItemService {

    InventoryItem getInventoryItem(String inventoryItemId);

    Pagination<InventoryItem> getInventoryItems(int offset, int count, String query);

    InventoryItem updateInventoryItem(InventoryItem item);

    InventoryItem createInventoryItem(InventoryItem item);
}
