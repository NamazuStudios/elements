package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.DistinctInventoryItem;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

import java.util.Map;

/**
 * Manages distinct items in the
 */
public interface DistinctInventoryItemService {

    /**
     * Creates a distinct inventory item, specifying the user, profile, and item id. Distinct inventory items may be
     * owned at either the profile or the user level and may be transferred. When creating one, another, or both may be
     * specified. However the profile, when specified, must match the user id.
     *
     * @param userId the user id owning the item, may be null if profile id is specified
     * @param profileId the profile of the profile owning the item, may be null if hte user id is specified
     * @param itemId the item id, must not be null
     * @param metadata the metadata, may be null
     * @return the {@link DistinctInventoryItem} as it was written to the database.
     */
    DistinctInventoryItem createDistinctInventoryItem(
            String userId,
            String profileId,
            String itemId,
            Map<String, Object> metadata);

    /**
     * Gets a specific distinct inventory item from the database.
     *
     * @param itemNameOrId the item name or id
     * @return the distinct item
     */
    DistinctInventoryItem getDistinctInventoryItem(String itemNameOrId);

    /**
     * Gets a listing of distinct inventory items from the database.
     *
     * @param offset the offset from the beginning of the dataset
     * @param count the count
     * @param userId the userid, may be null,
     * @return a pagination of inventory items.
     */
    Pagination<InventoryItem> getDistinctInventoryItems(int offset, int count,
                                                        String userId, String profileId);

    /**
     * Gets a listing of distinct inventory items from the database.
     *
     * @param offset the offset from the beginning of the dataset
     * @param count the count
     * @param userId the userid, may be null,
     * @return a pagination of inventory items.
     */
    Pagination<InventoryItem> getDistinctInventoryItems(int offset, int count,
                                                        String userId, String profileId, String query);

    /**
     * Updates a distinct inventory item.
     *
     * @param distinctInventoryItemId
     * @param userId
     * @param profileId
     * @param metadata
     * @return
     */
    DistinctInventoryItem updateDistinctInventoryItem(
            String distinctInventoryItemId,
            String userId,
            String profileId,
            Map<String, Object> metadata);

    /**
     * Deletes a specific distinct inventory item id.
     *
     * @param inventoryItemId
     */
    void deleteInventoryItem(String inventoryItemId);

}
