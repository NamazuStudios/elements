package dev.getelements.elements.service.inventory;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEntry;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType;
import dev.getelements.elements.sdk.service.inventory.ItemLedgerService;

public class AnonItemLedgerService implements ItemLedgerService {

    @Override
    public Pagination<ItemLedgerEntry> getLedgerEntries(
            final String inventoryItemId, final int offset, final int count,
            final ItemLedgerEventType eventType) {
        throw new ForbiddenException("Unprivileged requests cannot read the item ledger.");
    }

    @Override
    public Pagination<ItemLedgerEntry> getLedgerEntriesForUser(
            final String userId, final int offset, final int count,
            final ItemLedgerEventType eventType) {
        throw new ForbiddenException("Unprivileged requests cannot read the item ledger.");
    }
}
