package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.DistinctInventoryItem;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

import java.util.Map;

public interface DistinctInventoryItemDao {

    DistinctInventoryItem createDistinctInventoryItem(DistinctInventoryItem distinctInventoryItem);

    DistinctInventoryItem getDistinctInventoryItem(String itemNameOrId);

    Pagination<InventoryItem> getDistinctInventoryItems(
            int offset, int count,
            String userId, String profileId);

    Pagination<InventoryItem> getDistinctInventoryItems(
            int offset, int count,
            String userId, String profileId, String query);

    DistinctInventoryItem updateDistinctInventoryItem(DistinctInventoryItem distinctInventoryItem);

    void deleteDistinctInventoryItem(String inventoryItemId);
    
}
