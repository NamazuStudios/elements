package dev.getelements.elements.service.inventory;

import dev.getelements.elements.dao.InventoryItemDao;
import dev.getelements.elements.dao.ItemDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.inventory.InventoryItem;

import javax.inject.Inject;

import static dev.getelements.elements.dao.InventoryItemDao.SIMPLE_PRIORITY;

public class SuperUserSimpleInventoryItemService implements SimpleInventoryItemService {

    private UserDao userDao;

    private ItemDao itemDao;

    private InventoryItemDao inventoryItemDao;

    @Override
    public InventoryItem getInventoryItem(final String inventoryItemId) {
        return getInventoryItemDao().getInventoryItem(inventoryItemId);
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset, final int count, final String userId) {
        return userId == null || userId.trim().isEmpty() ?
            getInventoryItemDao().getInventoryItems(offset, count) :
            getUserDao().findActiveUser(userId)
                .map(user -> getInventoryItemDao().getInventoryItems(offset, count, user))
                .orElseGet(Pagination::new);
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset,
                                                       final int count,
                                                       final String userId,
                                                       final String query) {
        return userId == null || userId.trim().isEmpty() ?
            getInventoryItemDao().getInventoryItems(offset, count) :
            getUserDao().findActiveUser(userId)
                .map(user -> getInventoryItemDao().getInventoryItems(offset, count, user, query))
                .orElseGet(Pagination::new);
    }

    @Override
    public InventoryItem adjustInventoryItemQuantity(
            final String inventoryItemId, final String userId,
            final int quantityDelta) {
        return getInventoryItemDao().adjustQuantityForItem(inventoryItemId, quantityDelta);
    }

    @Override
    public InventoryItem createInventoryItem(final String userId, final String itemId, final int initialQuantity) {

        final var user = getUserDao().getActiveUser(userId);
        final var item = getItemDao().getItemByIdOrName(itemId);

        final var inventoryItem = new InventoryItem();

        inventoryItem.setUser(user);
        inventoryItem.setItem(item);
        inventoryItem.setPriority(SIMPLE_PRIORITY);
        inventoryItem.setQuantity(initialQuantity);

        return getInventoryItemDao().createInventoryItem(inventoryItem);

    }

    @Override
    public InventoryItem updateInventoryItem(
            final String inventoryItemId,
            final int quantity) {
        return getInventoryItemDao().updateInventoryItem(inventoryItemId, quantity);
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

    public InventoryItemDao getInventoryItemDao() {
        return inventoryItemDao;
    }

    @Inject
    public void setInventoryItemDao(InventoryItemDao inventoryItemDao) {
        this.inventoryItemDao = inventoryItemDao;
    }

}
