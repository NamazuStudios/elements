package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Created by davidjbrooks on 11/11/18.
 */
@Expose({
    @ExposedModuleDefinition("namazu.elements.dao.inventoryitem"),
    @ExposedModuleDefinition(
        value = "namazu.socialengine.dao.inventoryitem",
        deprecated = @DeprecationDefinition("Use namazu.elements.dao.inventoryitem instead"))
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
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link InventoryItem} objects.
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count);

    /**
     * Gets inventory items specifying the offset and the count.
     *
     * @param offset the offset
     * @param count the count
     * @param user the {@link User} that owns the items
     * @return a {@link Pagination} of {@link InventoryItem} objects.
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count, User user);

    /**
     * Gets inventory items specifying the offset and the count, specifying a search filter.
     *
     * @param offset the offset
     * @param count the count
     * @param user the {@link User} that owns the items
     * @param search a query to filter the results
     * @return a {@link Pagination} of {@link InventoryItem} objects.
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count, User user, String search);

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
     * @param inventoryItemId
     * @param quantity
     */
    InventoryItem updateInventoryItem(String inventoryItemId, int quantity);

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
     * @param quantity the amount to adjust the quantity by
     * @return the updated {@link InventoryItem}
     */
    InventoryItem setQuantityForItem(User user, String itemNameOrId, int priority, int quantity);

    /**
     * Adjusts the quantity of the supplied item and user.
     *
     * @param inventoryItemId the {@link Item#getName()} or {@link Item#getId()}
     * @param quantityDelta the amount to adjust the quantity by
     * @return the updated {@link InventoryItem}
     */
    InventoryItem adjustQuantityForItem(String inventoryItemId, int quantityDelta);

    /**
     * Convenience method which allows for invoking {@link #adjustQuantityForItem(String, int)} without needing to
     * create the item first.
     *
     * @param user the user object to adjust
     * @param itemNameOrId the item name or identifier
     * @param priority the priority slot
     * @param quantityDelta the quantity delta
     * @return the updated {@link InventoryItem}
     */
    InventoryItem adjustQuantityForItem(User user, String itemNameOrId, int priority, int quantityDelta);

    /**
     * Deletes an inventory item.
     *
     * @param inventoryItemId the {@link InventoryItem}'s id.
     */
    void deleteInventoryItem(String inventoryItemId);

}
