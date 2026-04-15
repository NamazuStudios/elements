package dev.getelements.elements.sdk.service.inventory;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEntry;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Read-only service for querying the item audit ledger.
 * Writes are performed internally by the inventory service implementations.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ItemLedgerService {

    /**
     * Returns ledger entries for a specific inventory item, most-recent first.
     *
     * @param inventoryItemId the inventory item ID
     * @param offset pagination offset
     * @param count maximum results
     * @param eventType optional filter; pass null to return all event types
     * @return paginated entries
     */
    Pagination<ItemLedgerEntry> getLedgerEntries(
            String inventoryItemId, int offset, int count, ItemLedgerEventType eventType);

    /**
     * Returns all ledger entries for a specific user (across all inventory items), most-recent first.
     *
     * @param userId the user ID
     * @param offset pagination offset
     * @param count maximum results
     * @param eventType optional filter; pass null to return all event types
     * @return paginated entries
     */
    Pagination<ItemLedgerEntry> getLedgerEntriesForUser(
            String userId, int offset, int count, ItemLedgerEventType eventType);

}
