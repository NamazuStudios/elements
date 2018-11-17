package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

import javax.inject.Inject;

public class UserSimpleInventoryItemService implements SimpleInventoryItemService {

    protected InventoryItemDao inventoryItemDao;

    @Override
    public InventoryItem getInventoryItem(String itemNameOrId) {
        return inventoryItemDao.getInventoryItemByItemNameOrId(itemNameOrId);
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(int offset, int count) {
        return inventoryItemDao.getInventoryItems(offset, count);
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(int offset, int count, String query) {
        return inventoryItemDao.getInventoryItems(offset, count, query);
    }


    @Override
    public InventoryItem adjustInventoryItemQuantity(String itemNameOrId, Integer quantityDelta)  { throw new ForbiddenException("Unprivileged requests are unable to modify inventory items."); }

    @Override
    public InventoryItem createInventoryItem(String itemNameOrId, Integer initialQuantity) { throw new ForbiddenException("Unprivileged requests are unable to create inventory items."); }

    @Override
    public void deleteInventoryItem(String itemNameOrId) { throw new ForbiddenException("Unprivileged requests are unable to delete inventory items."); }


    public InventoryItemDao getInventoryItemDao() {
        return inventoryItemDao;
    }

    @Inject
    public void setInventoryItemDao(InventoryItemDao inventoryItemDao) {
        this.inventoryItemDao = inventoryItemDao;
    }
}
