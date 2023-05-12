package dev.getelements.elements.dao;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.model.inventory.InventoryItem;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.util.Optional;

/**
 * Distinct inventory item Dao.
 */
@Expose({
        @ModuleDefinition("eci.elements.dao.distinctinventoryitem"),
        @ModuleDefinition(
                value = "namazu.elements.dao.distinctinventoryitem",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.distinctinventoryitem instead.")
        ),
})
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
     * Gets a listing distinct inventory tiems.
     *
     * @param offset the offset from the beginning of the dataset
     * @param count the number of items to return
     * @param userId the user id, if specified. Otherwise null.
     * @param profileId the profile id, if specified. Otherwise null.
     * @return a {@link Pagination<InventoryItem>}
     */
    Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            int offset, int count,
            String userId, String profileId);

    /**
     * Gets a listing distinct inventory items filtering by query string.
     *
     * @param offset the offset from the beginning of the dataset
     * @param count the number of items to return
     * @param userId the user id, if specified. Otherwise null.
     * @param profileId the profile id, if specified. Otherwise null.
     * @return a {@link Pagination<InventoryItem>}
     */
    default Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            int offset, int count,
            String userId, String profileId, String query) {
        return getDistinctInventoryItems(offset, count, userId, profileId);
    }

    /**
     * Updates a distinct inventory item.
     *
     * @param distinctInventoryItem the distinct inventory item
     * @retur the item, as written to the database
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
     * @Param itemId the {@link DistinctInventoryItem#getId()}
     * @param ownerId the owner of the ID (either user or profile)
     * @return the item
     */
    Optional<DistinctInventoryItem> findDistinctInventoryItemForOwner(String itemId, String ownerId);

}
