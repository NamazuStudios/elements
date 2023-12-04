package dev.getelements.elements.service.inventory;

import dev.getelements.elements.dao.DistinctInventoryItemDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.inventory.DistinctInventoryItemNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.largeobject.LargeObjectCdnUtils;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;

public class UserDistinctInventoryItemService implements DistinctInventoryItemService {

    private User user;

    private ProfileDao profileDao;

    private LargeObjectCdnUtils largeObjectCdnUtils;

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

        largeObjectCdnUtils.setProfileCdnUrl(item.getProfile());
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

        Pagination<DistinctInventoryItem> items = getDistinctInventoryItemDao().getDistinctInventoryItems(offset, count, resolvedUserId, profileId);
        items.getObjects().forEach(item -> largeObjectCdnUtils.setProfileCdnUrl(item.getProfile()));
        return items;
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

        Pagination<DistinctInventoryItem> items = getDistinctInventoryItemDao().getDistinctInventoryItems(offset, count, resolvedUserId, profileId, query);
        items.getObjects().forEach(item -> largeObjectCdnUtils.setProfileCdnUrl(item.getProfile()));
        return items;
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

    public LargeObjectCdnUtils getLargeObjectCdnUtils() {
        return largeObjectCdnUtils;
    }

    @Inject
    public void setLargeObjectCdnUtils(LargeObjectCdnUtils largeObjectCdnUtils) {
        this.largeObjectCdnUtils = largeObjectCdnUtils;
    }
}
