package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.user.User;

/**
 * Allows for modification of the inventory using the advanced API.
 */
public interface AdvancedInventoryItemService {

    /**
     * Returns the {@link InventoryItem} associated with the specified id.
     *
     * @param inventoryItemId the id of the as specified by {@link InventoryItem#getId()}
     * @return the {@link InventoryItem} associated with specified item
     */
    InventoryItem getInventoryItem(String inventoryItemId);

    /**
     * Returns a list of {@link InventoryItem} objects.
     *
     * @param offset the offset
     * @param count the count
     * @param userId the user id to filter, pass null or empty to fetch all
     * @return the list of {@link InventoryItem} instances
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count, String userId);

    /**
     * Returns a list of {@link InventoryItem} objects.
     *
     * @param offset the offset
     * @param count the count
     * @param userId the user id to filter, pass null or empty to fetch all
     * @param query the search query
     * @return the list of {@link InventoryItem} instances
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count, String userId, String query);

    /**
     * Adjusts the quantity of the {@link InventoryItem} associated with the specified {@Link Item}.
     *
     * @param inventoryItemId the value of {@link InventoryItem#getId()}
     * @param quantityDelta the amount by which to adjust the quantity of the {@Link InventoryItem}
     * @return the {@link InventoryItem} as it was written to the database
     */
    InventoryItem adjustInventoryItemQuantity(String inventoryItemId, int quantityDelta);

    /**
     * Creates a new {@link InventoryItem} for the specified {@link Item}.
     *
     * @param userId the user to own the {@link InventoryItem}
     * @param itemId the {@link Item#getName()} or {@link Item#getId()} to use.
     * @param quantity the initial quantity
     * @param priority the priority slot
     * @return the {@link InventoryItem} as it was written to the database
     */
    InventoryItem createInventoryItem(String userId, String itemId, int quantity, int priority);

    /**
     * Updates an inventory item with the supplied user id, item id, and quantity.
     *
     * @param inventoryItemId the inventory item id
     * @param userId the user id of the user owning the inventory item
     * @param itemId the item id of the user owning the inventory item
     * @param quantity the quantity to set
     * @return the updated {@link InventoryItem}
     */
    InventoryItem updateInventoryItem(String inventoryItemId, int quantity);

    /**
     * Deletes an {@link InventoryItem} from the given {@link User}'s inventory.
     *
     * @param inventoryItemId the {@link InventoryItem}'s id.
     */
    void deleteInventoryItem(String inventoryItemId);

}
