package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.DistinctInventoryItem;
import com.namazustudios.socialengine.model.inventory.InventoryItem;

import javax.inject.Inject;
import java.util.Map;

public class UserDistinctInventoryItemService implements DistinctInventoryItemService {

    private UserDao userDao;

    private ProfileDao profileDao;

    @Override
    public DistinctInventoryItem createDistinctInventoryItem(
            final String userId,
            final String profileId,
            final String itemId,
            final Map<String, Object> metadata) {
        return null;
    }

    @Override
    public DistinctInventoryItem getDistinctInventoryItem(final String itemNameOrId) {
        return null;
    }

    @Override
    public Pagination<InventoryItem> getDistinctInventoryItems(
            final int offset,
            final int count,
            final String userId,
            final String profileId) {
        return null;
    }

    @Override
    public Pagination<InventoryItem> getDistinctInventoryItems(
            final int offset,
            final int count,
            final String userId,
            final String profileId,
            final String query) {
        return null;
    }

    @Override
    public DistinctInventoryItem updateDistinctInventoryItem(
            final String distinctInventoryItemId,
            final String userId,
            final String profileId,
            final Map<String, Object> metadata) {
        return null;
    }

    @Override
    public void deleteInventoryItem(final String inventoryItemId) {

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

}
