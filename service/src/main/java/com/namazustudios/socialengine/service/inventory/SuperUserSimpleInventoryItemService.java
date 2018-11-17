package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

import javax.inject.Inject;
import javax.inject.Provider;

public class SuperUserSimpleInventoryItemService extends UserSimpleInventoryItemService implements SimpleInventoryItemService {

    private User user;

    private ItemDao itemDao;

    @Override
    public InventoryItem adjustInventoryItemQuantity(String itemNameOrId, Integer quantityDelta) {
        InventoryItem inventoryItem = inventoryItemDao.getInventoryItemByItemNameOrId(itemNameOrId);

        if(null == inventoryItem) {
            throw new NotFoundException();
        }

        inventoryItem.setQuantity(inventoryItem.getQuantity() + quantityDelta);

        if(inventoryItem.getQuantity() < 0) {
            throw new IllegalArgumentException("Final quantity may not be less than 0");
        }

        inventoryItemDao.updateInventoryItem(inventoryItem);

        return inventoryItem;
    }

    @Override
    public InventoryItem createInventoryItem(String itemNameOrId, Integer initialQuantity) {
        InventoryItem inventoryItem = new InventoryItem();

        inventoryItem.setPriority(0);
        inventoryItem.setUser(user);
        inventoryItem.setQuantity((null != initialQuantity) ? initialQuantity : 0);
        inventoryItem.setItem(itemDao.getItemByIdOrName(itemNameOrId));

        return inventoryItemDao.createInventoryItem(inventoryItem);
    }

    @Override
    public void deleteInventoryItem(String itemNameOrId) {
        InventoryItem inventoryItem = inventoryItemDao.getInventoryItemByItemNameOrId(itemNameOrId);

        if(null == inventoryItem) {
            throw new NotFoundException();
        }

        inventoryItemDao.deleteInventoryItem(inventoryItem.getId());
    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    @Inject
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }
}
