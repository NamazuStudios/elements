package dev.getelements.elements.service.inventory;

import dev.getelements.elements.dao.DistinctInventoryItemDao;
import dev.getelements.elements.dao.ItemDao;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.item.ItemNotFoundException;
import dev.getelements.elements.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.exception.user.UserNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.service.util.UserProfileUtility;

import javax.inject.Inject;
import java.util.Map;

public class SuperUserDistinctInventoryItemService implements DistinctInventoryItemService {

    private ItemDao itemDao;

    private UserProfileUtility userProfileUtility;

    private DistinctInventoryItemDao distinctInventoryItemDao;

    @Override
    public DistinctInventoryItem createDistinctInventoryItem(
            final String userId,
            final String profileId,
            final String itemId,
            final Map<String, Object> metadata) {

        final var distinctInventoryItem = new DistinctInventoryItem();

        try {
            final var item = getItemDao().getItemByIdOrName(itemId);
            distinctInventoryItem.setItem(item);
        } catch (ItemNotFoundException ex) {
            throw new InvalidDataException("Invalid item.", ex);
        }

        try {
            final var record = getUserProfileUtility().getAndCheckForMatch(profileId, userId);
            distinctInventoryItem.setUser(record.user);
            distinctInventoryItem.setProfile(record.profile);
        } catch (UserNotFoundException | ProfileNotFoundException ex) {
            throw new InvalidDataException("User or Profile.", ex);
        }

        distinctInventoryItem.setMetadata(metadata);

        return getDistinctInventoryItemDao().createDistinctInventoryItem(distinctInventoryItem);

    }

    @Override
    public DistinctInventoryItem getDistinctInventoryItem(final String itemNameOrId) {
        return getDistinctInventoryItemDao().getDistinctInventoryItem(itemNameOrId);
    }

    @Override
    public Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            final int offset,
            final int count,
            final String userId,
            final String profileId) {
        return getDistinctInventoryItemDao().getDistinctInventoryItems(offset, count, userId, profileId);
    }

    @Override
    public Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            final int offset,
            final int count,
            final String userId,
            final String profileId,
            final String query) {
        return getDistinctInventoryItemDao().getDistinctInventoryItems(offset, count, userId, profileId, query);
    }

    @Override
    public DistinctInventoryItem updateDistinctInventoryItem(
            final String distinctInventoryItemId,
            final String userId,
            final String profileId,
            final Map<String, Object> metadata) {

        final var distinctInventoryItem = getDistinctInventoryItemDao().getDistinctInventoryItem(distinctInventoryItemId);

        try {
            final var record = getUserProfileUtility().getAndCheckForMatch(profileId, userId);
            distinctInventoryItem.setUser(record.user);
            distinctInventoryItem.setProfile(record.profile);
        } catch (UserNotFoundException | ProfileNotFoundException ex) {
            throw new InvalidDataException("User or Profile.", ex);
        }

        distinctInventoryItem.setMetadata(metadata);

        return getDistinctInventoryItemDao().updateDistinctInventoryItem(distinctInventoryItem);

    }

    @Override
    public void deleteInventoryItem(final String inventoryItemId) {
        getDistinctInventoryItemDao().deleteDistinctInventoryItem(inventoryItemId);
    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    @Inject
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public UserProfileUtility getUserProfileUtility() {
        return userProfileUtility;
    }

    @Inject
    public void setUserProfileUtility(UserProfileUtility userProfileUtility) {
        this.userProfileUtility = userProfileUtility;
    }

    public DistinctInventoryItemDao getDistinctInventoryItemDao() {
        return distinctInventoryItemDao;
    }

    @Inject
    public void setDistinctInventoryItemDao(DistinctInventoryItemDao distinctInventoryItemDao) {
        this.distinctInventoryItemDao = distinctInventoryItemDao;
    }

}
