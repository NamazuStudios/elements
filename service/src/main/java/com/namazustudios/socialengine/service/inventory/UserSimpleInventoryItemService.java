package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.dao.InventoryItemDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

import javax.inject.Inject;

import static com.namazustudios.socialengine.dao.InventoryItemDao.SIMPLE_PRIORITY;

public class UserSimpleInventoryItemService implements SimpleInventoryItemService {

    private User user;

    private InventoryItemDao inventoryItemDao;

    @Override
    public InventoryItem getInventoryItem(final String itemNameOrId) {
        return getInventoryItemDao().getInventoryItemByItemNameOrId(getUser(), itemNameOrId, SIMPLE_PRIORITY);
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset,
                                                       final int count,
                                                       final String userId) {
        return getUser().getId().equals(userId) ?
            getInventoryItemDao().getInventoryItems(getUser(), offset, count) :
            Pagination.empty();
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset,
                                                       final int count,
                                                       final String userId,
                                                       final String query) {
        return getUser().getId().equals(userId) ?
            getInventoryItemDao().getInventoryItems(getUser(), offset, count) :
            Pagination.empty();
    }

    @Override
    public InventoryItem adjustInventoryItemQuantity(final String userId, final String itemNameOrId, final int quantityDelta)  {
        throw new ForbiddenException("Unprivileged requests are unable to modify inventory items.");
    }

    @Override
    public InventoryItem createInventoryItem(final String userId, final String itemNameOrId, final int initialQuantity) {
        throw new ForbiddenException("Unprivileged requests are unable to create inventory items.");
    }

    @Override
    public void deleteInventoryItem(final String inventoryItemId) {
        throw new ForbiddenException("Unprivileged requests are unable to delete inventory items.");
    }

    public InventoryItemDao getInventoryItemDao() {
        return inventoryItemDao;
    }

    @Inject
    public void setInventoryItemDao(InventoryItemDao inventoryItemDao) {
        this.inventoryItemDao = inventoryItemDao;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }
}
