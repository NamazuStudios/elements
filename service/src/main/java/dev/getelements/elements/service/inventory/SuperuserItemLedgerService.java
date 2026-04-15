package dev.getelements.elements.service.inventory;

import dev.getelements.elements.sdk.dao.ItemLedgerDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEntry;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType;
import dev.getelements.elements.sdk.service.inventory.ItemLedgerService;
import jakarta.inject.Inject;

public class SuperuserItemLedgerService implements ItemLedgerService {

    private ItemLedgerDao itemLedgerDao;

    @Override
    public Pagination<ItemLedgerEntry> getLedgerEntries(
            final String inventoryItemId, final int offset, final int count,
            final ItemLedgerEventType eventType) {
        return getItemLedgerDao().getLedgerEntries(inventoryItemId, offset, count, eventType);
    }

    @Override
    public Pagination<ItemLedgerEntry> getLedgerEntriesForUser(
            final String userId, final int offset, final int count,
            final ItemLedgerEventType eventType) {
        return getItemLedgerDao().getLedgerEntriesForUser(userId, offset, count, eventType);
    }

    public ItemLedgerDao getItemLedgerDao() {
        return itemLedgerDao;
    }

    @Inject
    public void setItemLedgerDao(final ItemLedgerDao itemLedgerDao) {
        this.itemLedgerDao = itemLedgerDao;
    }
}
