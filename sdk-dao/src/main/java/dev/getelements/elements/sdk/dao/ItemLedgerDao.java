package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEntry;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType;

/**
 * Append-only DAO for item audit ledger entries. No update or delete methods are provided;
 * records are immutable by design.
 */
@ElementServiceExport
public interface ItemLedgerDao {

    /**
     * Appends an immutable audit record to the ledger.
     *
     * @param entry the entry to record
     * @return the persisted entry with its generated ID
     */
    ItemLedgerEntry createLedgerEntry(ItemLedgerEntry entry);

    /**
     * Retrieves ledger entries for a specific inventory item, most-recent first.
     *
     * @param inventoryItemId the inventory item ID
     * @param offset pagination offset
     * @param count maximum results to return
     * @param eventType optional event type filter; null returns all event types
     * @return paginated ledger entries
     */
    Pagination<ItemLedgerEntry> getLedgerEntries(
            String inventoryItemId, int offset, int count, ItemLedgerEventType eventType);

    /**
     * Retrieves all ledger entries for a specific user (across all items), most-recent first.
     *
     * @param userId the user ID
     * @param offset pagination offset
     * @param count maximum results to return
     * @param eventType optional event type filter; null returns all event types
     * @return paginated ledger entries
     */
    Pagination<ItemLedgerEntry> getLedgerEntriesForUser(
            String userId, int offset, int count, ItemLedgerEventType eventType);

}
