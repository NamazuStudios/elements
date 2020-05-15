package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.goods.Item;

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
     *
     * @param user
     * @param itemNameOrId the value of {@link Item#getId()} or {@link Item#getName()}
     * @param quantityDelta the amount by which to adjust the quantity of the {@Link InventoryItem}
     * @return the {@link InventoryItem} as it was written to the database
     */
    InventoryItem adjustInventoryItemQuantity(User user, String itemNameOrId, int quantityDelta);

    /**
     * Creates a new {@link InventoryItem} for the specified {@link Item}.
     *
     * @param user the user to own the {@link InventoryItem}
     * @param item the {@link Item} to use
     * @param initialQuantity the initial quantity
     * @return the {@link InventoryItem} as it was written to the database
     */
    InventoryItem createInventoryItem(User user, Item item, int initialQuantity);

    /**
     * Deletes an {@link InventoryItem} from the given {@link User}'s inventory.
     *
     * @param inventoryItemId the {@link InventoryItem}'s id.
     */
    void deleteInventoryItem(String inventoryItemId);

}
