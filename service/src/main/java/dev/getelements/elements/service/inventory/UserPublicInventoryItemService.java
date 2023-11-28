package dev.getelements.elements.service.inventory;

import dev.getelements.elements.dao.InventoryItemDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.inventory.InventoryItem;

import javax.inject.Inject;

public class UserPublicInventoryItemService implements PublicInventoryItemService {

    private InventoryItemDao inventoryItemDao;

    @Override
    public Pagination<InventoryItem> getPublicInventoryItems(int offset, int count) {
        return inventoryItemDao.getPublicInventoryItems(offset, count);
    }

    public InventoryItemDao getInventoryItemDao() {
        return inventoryItemDao;
    }

    @Inject
    public void setInventoryItemDao(InventoryItemDao inventoryItemDao) {
        this.inventoryItemDao = inventoryItemDao;
    }
}
