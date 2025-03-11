package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Optional;

/**
 * Distinct inventory item Dao.
 */

@ElementServiceExport
public interface DistinctInventoryItemDao {

    /**
     * Creates a distinct inventory item.
     *
     * @param distinctInventoryItem the distinct inventory item.
     * @return a distinct inventory item
     */
    DistinctInventoryItem createDistinctInventoryItem(DistinctInventoryItem distinctInventoryItem);

    /**
     * Creates a distinct inventory item.
     *
     * @param id the distinct inventory item.
     * @return a distinct inventory item
     */
    DistinctInventoryItem getDistinctInventoryItem(String id);

    /**
     * Gets a listing distinct inventory items.
     *
     * @param offset     the offset from the beginning of the dataset
     * @param count      the number of items to return
     * @param userId     checked id of user
     * @param profileId  checked id of profile.
     * @param publicOnly mark inventory items that are referred to only this with publicVisible flag
     * @return a {@link Pagination<InventoryItem>}
     */
    Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            int offset, int count,
            String userId, String profileId,
            boolean publicOnly);

    /**
     * Gets a listing distinct inventory items filtering by query string.
     *
     * @param offset     the offset from the beginning of the dataset
     * @param count      the number of items to return
     * @param userId     checked id of user
     * @param profileId  checked id of profile.
     * @param publicOnly mark inventory items that are referred to only this with publicVisible flag
     * @return a {@link Pagination<InventoryItem>}
     */
    Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            int offset, int count,
            String userId, String profileId,
            boolean publicOnly, String queryString);

    /**
     * Gets a total number of items for provided userId or profileId
     *
     * @param userId     checked id of user
     * @param profileId  checked id of profile.
     * @param publicOnly mark inventory items that are referred to only this with publicVisible flag
     * @return a number of items
     */
    Long getTotalDistinctInventoryItems(
            String userId, String profileId,
            boolean publicOnly, String queryString);

    /**
     * Gets a total number of distinct values for provided metadata field name.
     *
     * @param profileId optional value to count items that belongs only to that profile
     * @param fieldName metadata field name
     * @return a number of distinct values
     */
    Long countDistinctMetadataField(String profileId, String fieldName);

    /**
     * Updates a distinct inventory item.
     *
     * @param distinctInventoryItem the distinct inventory item
     * @return the item, as written to the database
     */
    DistinctInventoryItem updateDistinctInventoryItem(DistinctInventoryItem distinctInventoryItem);

    /**
     * Deletes a distinct inventory item.
     *
     * @param inventoryItemId the distinct inventory item
     */
    void deleteDistinctInventoryItem(String inventoryItemId);

    /**
     * Finds the distinct inventory item with the owner ID and owner ID. This can be used for an ownership check of
     * the specified item.
     *
     * @param itemId the {@link DistinctInventoryItem#getId()}
     * @param ownerId the owner of the ID (either user or profile)
     * @return the item
     */
    Optional<DistinctInventoryItem> findDistinctInventoryItemForOwner(String itemId, String ownerId);

}
