package dev.getelements.elements.service.inventory;

import dev.getelements.elements.sdk.dao.DistinctInventoryItemDao;
import dev.getelements.elements.sdk.dao.ItemDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.item.ItemNotFoundException;
import dev.getelements.elements.sdk.model.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.sdk.model.exception.user.UserNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.inventory.DistinctInventoryItemService;
import dev.getelements.elements.service.largeobject.LargeObjectCdnUtils;
import dev.getelements.elements.service.util.UserProfileUtility;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class SuperUserDistinctInventoryItemService implements DistinctInventoryItemService {

    private UserDao userDao;

    private User user;

    private ProfileDao profileDao;

    private ItemDao itemDao;

    private UserProfileUtility userProfileUtility;

    private DistinctInventoryItemDao distinctInventoryItemDao;

    private LargeObjectCdnUtils largeObjectCdnUtils;

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

        DistinctInventoryItem createdItem = getDistinctInventoryItemDao().createDistinctInventoryItem(distinctInventoryItem);
        return getLargeObjectCdnUtils().setDistinctItemProfileCdnUrl(createdItem);
    }

    @Override
    public DistinctInventoryItem getDistinctInventoryItem(final String itemNameOrId) {
        DistinctInventoryItem item = getDistinctInventoryItemDao().getDistinctInventoryItem(itemNameOrId);
        return getLargeObjectCdnUtils().setDistinctItemProfileCdnUrl(item);
    }

    @Override
    public Pagination<DistinctInventoryItem> getDistinctInventoryItems(
            final int offset,
            final int count,
            final String userId,
            final String profileId) {
        Pagination<DistinctInventoryItem> items = getDistinctInventoryItems(offset, count, userId, profileId, null);
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
        final Optional<Profile> profile = getProfileDao().findActiveProfile(profileId);
        if (profile.isPresent()) {
            final User user = isCurrentUser(userId) ? getUser() : getUserDao().getUser(userId);
            if (!user.getId().equals(profile.get().getUser().getId())) {
                return new Pagination<>();
            }
        }

        var publicOnly = !isCurrentUser(userId) && !isSuperUser();

        Pagination<DistinctInventoryItem> items = getDistinctInventoryItemDao().getDistinctInventoryItems(offset, count, userId, profileId, publicOnly, query);
        items.getObjects().forEach(item -> getLargeObjectCdnUtils().setDistinctItemProfileCdnUrl(item));
        return items;
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

        DistinctInventoryItem updatedItem = getDistinctInventoryItemDao().updateDistinctInventoryItem(distinctInventoryItem);
        return getLargeObjectCdnUtils().setDistinctItemProfileCdnUrl(updatedItem);
    }

    @Override
    public void deleteInventoryItem(final String inventoryItemId) {
        getDistinctInventoryItemDao().deleteDistinctInventoryItem(inventoryItemId);
    }

    private boolean isCurrentUser(String userId) {
        return isBlank(userId) || getUser().getId().equals(userId);
    }

    private boolean isSuperUser() {
        return getUser().getLevel() == User.Level.SUPERUSER;
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

    public LargeObjectCdnUtils getLargeObjectCdnUtils() {
        return largeObjectCdnUtils;
    }

    @Inject
    public void setLargeObjectCdnUtils(LargeObjectCdnUtils largeObjectCdnUtils) {
        this.largeObjectCdnUtils = largeObjectCdnUtils;
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
}
