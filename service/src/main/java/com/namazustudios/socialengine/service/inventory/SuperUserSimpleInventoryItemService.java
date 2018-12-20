package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

public class SuperUserSimpleInventoryItemService extends UserSimpleInventoryItemService implements SimpleInventoryItemService {

    @Override
    public InventoryItem adjustInventoryItemQuantity(
            final User user,
            final String itemNameOrId,
            final int quantityDelta) {
        return getInventoryItemDao().adjustQuantityForItem(user, itemNameOrId, InventoryItemDao.SIMPLE_PRIORITY, quantityDelta);
    }

    @Override
    public InventoryItem createInventoryItem(final User user, final Item item, final int initialQuantity) {
        final InventoryItem inventoryItem = new InventoryItem();

        inventoryItem.setUser(user);
        inventoryItem.setItem(item);
        inventoryItem.setPriority(InventoryItemDao.SIMPLE_PRIORITY);
        inventoryItem.setQuantity(initialQuantity);

        return getInventoryItemDao().createInventoryItem(inventoryItem);
    }

}
