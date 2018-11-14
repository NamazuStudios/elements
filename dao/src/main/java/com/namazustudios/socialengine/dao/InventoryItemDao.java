package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
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
     * Gets inventory items specifying the offset and the count.
     *
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link InventoryItem} objects.
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count);

    /**
     * Gets inventory items specifying the offset and the count, specifying a search filter.
     *
     * @param offset the offset
     * @param count the count
     * @param search a query to filter the results
     * @return a {@link Pagination} of {@link InventoryItem} objects.
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count, String search);

    /**
     * Gets the specific inventory item with the id, or throws a {@link NotFoundException} if the
     * profile can't be found.
     *
     * @return the {@link InventoryItem} that was requested, never null
     */
    InventoryItem getInventoryItem(String inventoryItemId);

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
     * Deletes an inventory item.
     *
     * @param inventoryItemId the inventory item ID
     */
    void deleteInventoryItem(String inventoryItemId);
}
