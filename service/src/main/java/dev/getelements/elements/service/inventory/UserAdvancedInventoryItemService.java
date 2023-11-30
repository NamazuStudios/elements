package dev.getelements.elements.service.inventory;

import dev.getelements.elements.dao.InventoryItemDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.inventory.InventoryItem;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.exception.BadParameterException;
import dev.getelements.elements.service.AdvancedInventoryItemService;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class UserAdvancedInventoryItemService implements AdvancedInventoryItemService {

    private UserDao userDao;

    private User user;

    private InventoryItemDao inventoryItemDao;

    @Override
    public InventoryItem getInventoryItem(final String inventoryItemId) {
        return getInventoryItemDao().getInventoryItem(inventoryItemId);
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset, final int count, final String userId) {
        return getInventoryItems(offset, count, userId, "");
    }

    @Override
    public Pagination<InventoryItem> getInventoryItems(final int offset,
                                                       final int count,
                                                       final String userId,
                                                       final String query) {
        if (isBlank(userId)) {
            throw new BadParameterException("UserId must be provided.");
        }
        User user = getUserDao().getActiveUser(userId);
        return isCurrentUser(userId) ?
                getInventoryItemDao().getInventoryItems(offset, count, user, query) :
                getInventoryItemDao().getUserPublicInventoryItems(offset, count, user);
    }

    @Override
    public InventoryItem adjustInventoryItemQuantity(final String inventoryItemId,
                                                     final int quantityDelta) {
        throw new ForbiddenException("Unprivileged requests are unable to modify inventory items.");
    }

    @Override
    public InventoryItem updateInventoryItem(
            final String inventoryItemId,
            final int quantity) {
        throw new ForbiddenException("Unprivileged requests are unable to modify inventory items.");
    }

    @Override
    public InventoryItem createInventoryItem(final String userId,
                                             final String itemId,
                                             final int quantity,
                                             final int priority) {
        throw new ForbiddenException("Unprivileged requests are unable to modify inventory items.");
    }

    @Override
    public void deleteInventoryItem(final String inventoryItemId) {
        throw new ForbiddenException("Unprivileged requests are unable to modify inventory items.");
    }

    private boolean isCurrentUser(String userId) {
        return getUser().getId().equals(userId);
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

    public InventoryItemDao getInventoryItemDao() {
        return inventoryItemDao;
    }

    @Inject
    public void setInventoryItemDao(InventoryItemDao inventoryItemDao) {
        this.inventoryItemDao = inventoryItemDao;
    }

}
