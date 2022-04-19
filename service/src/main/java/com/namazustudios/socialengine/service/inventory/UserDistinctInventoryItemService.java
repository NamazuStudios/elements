package com.namazustudios.socialengine.service.inventory;

import com.namazustudios.socialengine.dao.DistinctInventoryItemDao;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.inventory.DistinctInventoryItemNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.DistinctInventoryItem;
import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;

public class UserDistinctInventoryItemService implements DistinctInventoryItemService {

    private User user;

    private ProfileDao profileDao;

    private DistinctInventoryItemDao distinctInventoryItemDao;

    @Override
    public DistinctInventoryItem createDistinctInventoryItem(
            final String userId,
            final String profileId,
            final String itemId,
            final Map<String, Object> metadata) {
        throw new ForbiddenException();
    }

    @Override
    public DistinctInventoryItem getDistinctInventoryItem(final String itemNameOrId) {
        final var item = getDistinctInventoryItemDao().getDistinctInventoryItem(itemNameOrId);

        if (!Objects.equals(getUser(). getId(), item.getUser().getId())) {
            throw new DistinctInventoryItemNotFoundException("Distinct inventory item not found: " + item.getId());
        }

        return item;
    }

    @Override
    public Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            final int offset,
            final int count,
            final String userId,
            final String profileId) {

        String resolvedUserId;

        if (userId == null) {
            resolvedUserId = getUser().getId();
        } else if (Objects.equals(getUser().getId(), userId)) {
            resolvedUserId = getUser().getId();
        }  else {
            return new Pagination<>();
        }

        if (profileId != null) {

            final var valid = getProfileDao()
                    .findActiveProfile(profileId)
                    .map(p -> Objects.equals(getUser().getId(), p.getUser().getId()))
                    .orElse(false);

            if (!valid) {
                return new Pagination<>();
            }

        }

        return getDistinctInventoryItemDao().getDistinctInventoryItems(offset, count, resolvedUserId, profileId);
    }

    @Override
    public Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            final int offset,
            final int count,
            final String userId,
            final String profileId,
            final String query) {

        String resolvedUserId;

        if (userId == null) {
            resolvedUserId = getUser().getId();
        } else if (Objects.equals(getUser().getId(), userId)) {
            resolvedUserId = getUser().getId();
        }  else {
            return new Pagination<>();
        }

        if (profileId != null) {

            final var valid = getProfileDao()
                .findActiveProfile(profileId)
                .map(p -> Objects.equals(getUser().getId(), p.getUser().getId()))
                .orElse(false);

            if (!valid) {
                return new Pagination<>();
            }

        }

        return getDistinctInventoryItemDao().getDistinctInventoryItems(offset, count, resolvedUserId, profileId, query);

    }

    @Override
    public DistinctInventoryItem updateDistinctInventoryItem(
            final String distinctInventoryItemId,
            final String userId,
            final String profileId,
            final Map<String, Object> metadata) {
        throw new ForbiddenException();
    }

    @Override
    public void deleteInventoryItem(final String inventoryItemId) {
        throw new ForbiddenException();
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public DistinctInventoryItemDao getDistinctInventoryItemDao() {
        return distinctInventoryItemDao;
    }

    @Inject
    public void setDistinctInventoryItemDao(DistinctInventoryItemDao distinctInventoryItemDao) {
        this.distinctInventoryItemDao = distinctInventoryItemDao;
    }

}
