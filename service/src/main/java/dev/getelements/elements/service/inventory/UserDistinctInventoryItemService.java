package dev.getelements.elements.service.inventory;

import com.google.common.base.Strings;
import dev.getelements.elements.sdk.dao.DistinctInventoryItemDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.inventory.DistinctInventoryItemNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.inventory.DistinctInventoryItemService;
import dev.getelements.elements.service.largeobject.LargeObjectCdnUtils;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class UserDistinctInventoryItemService implements DistinctInventoryItemService {

    private User user;

    private UserDao userDao;

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

        if (!item.getItem().isPublicVisible() && !Objects.equals(getUser(). getId(), item.getUser().getId())) {
            throw new DistinctInventoryItemNotFoundException("Distinct inventory item not found: " + item.getId());
        }

        return largeObjectCdnUtils.setDistinctItemProfileCdnUrl(item);
    }

    @Override
    public Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            final int offset,
            final int count,
            final String userId,
            final String profileId) {

        Pagination<DistinctInventoryItem> items =  getDistinctInventoryItems(offset, count, userId, profileId, null);
        items.getObjects().forEach(item -> getLargeObjectCdnUtils().setDistinctItemProfileCdnUrl(item));
        return items;
    }

    @Override
    public Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            final int offset,
            final int count,
            final String userId,
            final String profileId,
            final String query) {

        final String resolvedUserId = Strings.isNullOrEmpty(userId) ? getUser().getId() : userId;

        final Optional<Profile> profile = getProfileDao().findActiveProfile(profileId);
        if (profile.isPresent()) {
            final User user = isCurrentUser(userId) ? getUser() : getUserDao().getUser(userId);
            if (!user.getId().equals(profile.get().getUser().getId())) {
                return new Pagination<>();
            }
        }
        Pagination<DistinctInventoryItem> items =  getDistinctInventoryItemDao().getDistinctInventoryItems(offset, count, resolvedUserId, profileId, !isCurrentUser(userId), query);
        items.getObjects().forEach(item -> getLargeObjectCdnUtils().setDistinctItemProfileCdnUrl(item));
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

    private boolean isCurrentUser(String userId) {
        return isBlank(userId) || getUser().getId().equals(userId);
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
