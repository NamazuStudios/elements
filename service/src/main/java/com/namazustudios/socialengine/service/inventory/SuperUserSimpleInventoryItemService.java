package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

import javax.inject.Inject;

public class SuperUserSimpleInventoryItemService extends UserSimpleInventoryItemService implements SimpleInventoryItemService {

    private ItemDao itemDao;

    @Override
    public InventoryItem adjustInventoryItemQuantity(String itemNameOrId, Integer quantityDelta) {
        // find the inventory item by item name or id

        return null;
    }

    @Override
    public InventoryItem createInventoryItem(String itemNameOrId, Integer initialQuantity) {
        InventoryItem inventoryItem = new InventoryItem();

        inventoryItem.setQuantity((null != initialQuantity) ? initialQuantity : 0);
        inventoryItem.setItem(itemDao.getItemByIdOrName(itemNameOrId));

        return inventoryItemDao.createInventoryItem(inventoryItem);
    }

    @Override
    public void deleteInventoryItem(String itemNameOrId) {
        // find the inventory item by item name or id

    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    @Inject
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }
}
