package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

import java.util.Set;

public interface SimpleInventoryItemService {

    /**
     * Creates, or updates, a new {@link Score}.  If the profile submitted with the the {@link Score} matches an
     * existing {@link Leaderboard} and {@link Profile}, then the existing record will be updated.
     *
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param score the {@link Score}
     * @return the {@link Score} as it was written to the database
     */
    InventoryItem getInventoryItem(String itemId);

    Pagination<InventoryItem> getInventoryItems(int offset, int count, String query);

    InventoryItem adjustInventoryItemQuantity(String itemId, Integer quantityDelta);

    InventoryItem createInventoryItem(InventoryItem item);

    void deleteInventoryItem(String itemId);
}
