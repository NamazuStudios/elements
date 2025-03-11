package dev.getelements.elements.service.inventory;

import dev.getelements.elements.sdk.dao.InventoryItemDao;
import dev.getelements.elements.sdk.dao.ItemDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.inventory.SimpleInventoryItemService;
import jakarta.inject.Inject;

import static dev.getelements.elements.sdk.dao.InventoryItemDao.SIMPLE_PRIORITY;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SuperUserSimpleInventoryItemService implements SimpleInventoryItemService {

    private UserDao userDao;

    private User user;

    private ItemDao itemDao;

    private InventoryItemDao inventoryItemDao;

    @Override
    public InventoryItem getInventoryItem(final String inventoryItemId) {
        return getInventoryItemDao().getInventoryItem(inventoryItemId);
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset, final int count, final String userId) {
        return getInventoryItems(offset, count, userId, null);
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset,
                                                       final int count,
                                                       final String userId,
                                                       final String query) {
        final User user = isCurrentUser(userId) ? getUser() : getUserDao().getUser(userId);
        return isCurrentUser(userId) ?
                getInventoryItemDao().getInventoryItems(offset, count, user, query) :
                getInventoryItemDao().getUserPublicInventoryItems(offset, count, user);
    }

    @Override
    public InventoryItem adjustInventoryItemQuantity(
            final String inventoryItemId, final String userId,
            final int quantityDelta) {
        return getInventoryItemDao().adjustQuantityForItem(inventoryItemId, quantityDelta);
    }

    @Override
    public InventoryItem createInventoryItem(final String userId, final String itemId, final int initialQuantity) {

        final var user = getUserDao().getUser(userId);
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

    private boolean isCurrentUser(String userId) {
        return isBlank(userId) || getUser().getId().equals(userId);
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

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }
}
