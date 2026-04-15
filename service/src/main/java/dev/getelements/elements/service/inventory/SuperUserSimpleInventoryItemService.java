package dev.getelements.elements.service.inventory;

import dev.getelements.elements.sdk.dao.InventoryItemDao;
import dev.getelements.elements.sdk.dao.ItemDao;
import dev.getelements.elements.sdk.dao.ItemLedgerDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEntry;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType;
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

    private ItemLedgerDao itemLedgerDao;

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
        final InventoryItem result = getInventoryItemDao().adjustQuantityForItem(inventoryItemId, quantityDelta);
        final var entry = newFungibleEntry(result, ItemLedgerEventType.QUANTITY_ADJUSTED);
        entry.setQuantityBefore(result.getQuantity() - quantityDelta);
        entry.setQuantityAfter(result.getQuantity());
        getItemLedgerDao().createLedgerEntry(entry);
        return result;
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

        final InventoryItem result = getInventoryItemDao().createInventoryItem(inventoryItem);
        final var entry = newFungibleEntry(result, ItemLedgerEventType.CREATED);
        entry.setQuantityBefore(0);
        entry.setQuantityAfter(result.getQuantity());
        getItemLedgerDao().createLedgerEntry(entry);
        return result;
    }

    @Override
    public InventoryItem updateInventoryItem(
            final String inventoryItemId,
            final int quantity) {
        final InventoryItem result = getInventoryItemDao().updateInventoryItem(inventoryItemId, quantity);
        final var entry = newFungibleEntry(result, ItemLedgerEventType.QUANTITY_SET);
        entry.setQuantityAfter(quantity);
        getItemLedgerDao().createLedgerEntry(entry);
        return result;
    }

    @Override
    public void deleteInventoryItem(final String inventoryItemId) {
        final InventoryItem existing = getInventoryItemDao().getInventoryItem(inventoryItemId);
        getInventoryItemDao().deleteInventoryItem(inventoryItemId);
        getItemLedgerDao().createLedgerEntry(
                newFungibleEntry(inventoryItemId, existing.getUser().getId(),
                        existing.getItem().getId(), ItemLedgerEventType.DELETED));
    }

    private ItemLedgerEntry newFungibleEntry(final InventoryItem item, final ItemLedgerEventType eventType) {
        return newFungibleEntry(item.getId(), item.getUser().getId(), item.getItem().getId(), eventType);
    }

    private ItemLedgerEntry newFungibleEntry(final String inventoryItemId,
                                             final String userId,
                                             final String itemId,
                                             final ItemLedgerEventType eventType) {
        final var entry = new ItemLedgerEntry();
        entry.setInventoryItemId(inventoryItemId);
        entry.setItemCategory(ItemCategory.FUNGIBLE);
        entry.setItemId(itemId);
        entry.setUserId(userId);
        entry.setActorId(getUser() != null ? getUser().getId() : null);
        entry.setEventType(eventType);
        return entry;
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

    public ItemLedgerDao getItemLedgerDao() {
        return itemLedgerDao;
    }

    @Inject
    public void setItemLedgerDao(final ItemLedgerDao itemLedgerDao) {
        this.itemLedgerDao = itemLedgerDao;
    }
}
