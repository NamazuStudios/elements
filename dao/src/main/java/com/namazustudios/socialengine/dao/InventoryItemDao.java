package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;

/**
 * Created by davidjbrooks on 11/11/18.
 */
@Expose(modules = {
    "namazu.elements.dao.inventoryitem",
    "namazu.socialengine.dao.inventoryitem",
})
public interface InventoryItemDao {

    /**
     * The priority for use by the {@link InventoryItemDao};
     */
    int SIMPLE_PRIORITY = 0;

    /**
     * Gets the specific inventory item with the id, or throws a {@link NotFoundException} if the
     * inventory item can't be found.
     *
     * @return the {@link InventoryItem} that was requested, never null
     */
    InventoryItem getInventoryItem(String inventoryItemId);

    /**
     * Gets inventory items specifying the offset and the count.
     *
     * @param user the {@link User} that owns the items
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link InventoryItem} objects.
     */
    Pagination<InventoryItem> getInventoryItems(User user, int offset, int count);

    /**
     * Gets inventory items specifying the offset and the count, specifying a search filter.
     *
     * @param user the {@link User} that owns the items
     * @param offset the offset
     * @param count the count
     * @param search a query to filter the results
     * @return a {@link Pagination} of {@link InventoryItem} objects.
     */
    Pagination<InventoryItem> getInventoryItems(User user, int offset, int count, String search);

    /**
     * Gets the primary (single) inventory item for with the item name or id, or throws a {@link NotFoundException}
     * if the item or inventory item can't be found.  This uses the {@link #SIMPLE_PRIORITY} slot.
     *
     * @param user the {@link User} that owns the item
     * @param itemNameOrId an item name or ID to limit the results
     * @return the {@link InventoryItem} that was requested, never null
     */
    default InventoryItem getInventoryItemByItemNameOrId(User user, String itemNameOrId) {
        return getInventoryItemByItemNameOrId(user, itemNameOrId, SIMPLE_PRIORITY);
    }

    /**
     * Gets the primary (single) inventory item for with the item name or id, or throws a {@link NotFoundException}
     * if the item or inventory item can't be found.
     *
     * @param user the {@link User} that owns the item
     * @param itemNameOrId an item name or ID to limit the results
     * @param priority
     * @return the {@link InventoryItem} that was requested, never null
     */
    InventoryItem getInventoryItemByItemNameOrId(User user, String itemNameOrId, int priority);

    /**
     * Updates the specific inventory item with the id, or throws a {@link NotFoundException} if the
     * inventory item can't be found.  The {@link InventoryItem#getId()} is used to key the inventory item being updated.
     *
     * @return the {@link InventoryItem} as it was written into the database
     * @throws InvalidDataException
     *     if the state of the passed in InventoryItem is invalid
     */
    InventoryItem updateInventoryItem(InventoryItem inventoryItem);

    /**
     * Creates an inventory item.  The value of {@link InventoryItem#getId()} will be ignored.
     *
     * @return the {@link InventoryItem} as it was written into the database
     * @throws InvalidDataException
     *     if the state of the passed in InventoryItem is invalid
     * @throws DuplicateException
     *     if the passed in Item has a name that already exists
     */
    InventoryItem createInventoryItem(InventoryItem inventoryItem);

    /**
     * Adjusts the quantity of the supplied item and user.
     *
     * @param user the {@link User} for which to adjust the item.
     * @param itemNameOrId the {@link Item#getName()} or {@link Item#getId()}
     * @param priority the priority of the item slot
     * @param quantityDelta the amount to adjust the quantity by
     * @return the updated {@link InventoryItem}
     */
    InventoryItem adjustQuantityForItem(User user, String itemNameOrId, int priority, int quantityDelta);

    /**
     * Deletes an inventory item using the value of {@link InventoryItem#getId()}.
     *
     * @param inventoryItemId the inventory item ID
     */
    void deleteInventoryItem(String inventoryItemId);

}
