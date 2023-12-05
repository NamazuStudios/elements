package dev.getelements.elements.service.inventory;

import dev.getelements.elements.dao.DistinctInventoryItemDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.inventory.DistinctInventoryItemNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class UserDistinctInventoryItemService implements DistinctInventoryItemService {

    private UserDao userDao;

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

        return getDistinctInventoryItems(offset, count, userId, profileId, null);
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
            final User user = isCurrentUser(userId) ? getUser() : getUserDao().getActiveUser(userId);
            if (!user.getId().equals(profile.get().getUser().getId())) {
                return new Pagination<>();
            }
        }
        return getDistinctInventoryItemDao().getDistinctInventoryItems(offset, count, profileId, userId, isCurrentUser(userId), query);
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

}
