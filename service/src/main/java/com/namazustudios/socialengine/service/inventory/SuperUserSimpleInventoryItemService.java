package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

import javax.inject.Inject;

public class SuperUserSimpleInventoryItemService extends UserSimpleInventoryItemService implements SimpleInventoryItemService {

    private UserDao userDao;

    private ItemDao itemDao;

    @Override
    public InventoryItem adjustInventoryItemQuantity(
            final String userId,
            final String itemNameOrId,
            final int quantityDelta) {

        return getUserDao()
            .findActiveUser(userId)
            .map(uid -> getInventoryItemDao().adjustQuantityForItem(
                    uid, itemNameOrId,
                    InventoryItemDao.SIMPLE_PRIORITY, quantityDelta
                )
            ).orElseThrow(UserNotFoundException::new);

    }

    @Override
    public InventoryItem createInventoryItem(final String userId, final String itemId, final int initialQuantity) {

        final var user = getUserDao().getActiveUser(userId);
        final var item = getItemDao().getItemByIdOrName(itemId);

        final var inventoryItem = new InventoryItem();

        inventoryItem.setUser(user);
        inventoryItem.setItem(item);
        inventoryItem.setPriority(InventoryItemDao.SIMPLE_PRIORITY);
        inventoryItem.setQuantity(initialQuantity);

        return getInventoryItemDao().createInventoryItem(inventoryItem);
    }

    @Override
    public void deleteInventoryItem(final String inventoryItemId) {
        getInventoryItemDao().deleteInventoryItem(inventoryItemId);
    }


    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    @Inject
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

}
