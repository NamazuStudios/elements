package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.dao.DistinctInventoryItemDao;
import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.item.ItemNotFoundException;
import com.namazustudios.socialengine.exception.profile.ProfileNotFoundException;
import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.DistinctInventoryItem;
import com.namazustudios.socialengine.service.util.UserProfileUtility;

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
            final var record = getUserProfileUtility().getAndCheckForMatch(userId, profileId);
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
            final var record = getUserProfileUtility().getAndCheckForMatch(userId, profileId);
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
