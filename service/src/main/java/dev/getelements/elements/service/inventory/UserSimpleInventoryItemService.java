package dev.getelements.elements.service.inventory;

import dev.getelements.elements.dao.InventoryItemDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.inventory.InventoryItem;
import dev.getelements.elements.model.user.User;

import javax.inject.Inject;

public class UserSimpleInventoryItemService implements SimpleInventoryItemService {

    private User user;

    private InventoryItemDao inventoryItemDao;

    @Override
    public InventoryItem getInventoryItem(final String inventoryItemId) {
        return getInventoryItemDao().getInventoryItem(inventoryItemId);
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset, final int count, final String userId) {
        return userId == null || getUser().getId().equals(userId.trim()) ?
            getInventoryItemDao().getInventoryItems(offset, count, getUser()) :
            Pagination.empty();
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset,
                                                       final int count,
                                                       final String userId,
                                                       final String query) {
        return userId == null || getUser().getId().equals(userId.trim()) ?
            getInventoryItemDao().getInventoryItems(offset, count, getUser(), query) :
            Pagination.empty();
    }

    @Override
    public InventoryItem updateInventoryItem(final String inventoryItemId, final int quantity) {
        throw new ForbiddenException("Unprivileged requests are unable to modify inventory items.");
    }

    @Override
    public InventoryItem adjustInventoryItemQuantity(final String inventoryItemId, final String userId, final int quantityDelta)  {
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
