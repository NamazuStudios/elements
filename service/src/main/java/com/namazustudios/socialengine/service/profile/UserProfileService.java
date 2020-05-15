package com.namazustudios.socialengine.service.profile;


import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.ProfileService;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class UserProfileService implements ProfileService {

    private User user;

    private UserService userService;

    private ProfileDao profileDao;

    private Supplier<Profile> currentProfileSupplier;

    @Override
    public Pagination<Profile> getProfiles(final int offset, final int count,
                                           final String applicationNameOrId, final String userId,
                                           final Long lowerBoundTimestamp, final Long upperBoundTimestamp) {
        if (getUserService().isCurrentUserAlias(userId)) {
            return getProfileDao()
                .getActiveProfiles(
                        offset, count,
                        applicationNameOrId, getUserService().getCurrentUser().getId(),
                        lowerBoundTimestamp, upperBoundTimestamp)
                .transform(this::redactPrivateInformation);
        } else if (userId == null || getUserService().isCurrentUser(userId)) {
            return getProfileDao()
                .getActiveProfiles(
                    offset, count,
                    applicationNameOrId, userId,
                    lowerBoundTimestamp, upperBoundTimestamp)
                .transform(this::redactPrivateInformation);
        } else {
            return new Pagination<>();
        }
    }

    @Override
    public Pagination<Profile> getProfiles(
            int offset,
            int count,
            String search) {
        return getProfileDao()
            .getActiveProfiles(offset, count, search)
            .transform(this::redactPrivateInformation);
    }

    @Override
    public Profile getProfile(String profileId) {
        return getProfileDao().getActiveProfile(profileId);
    }

    @Override
    public Profile getCurrentProfile() {
        return getCurrentProfileSupplier().get();
    }

    @Override
    public Profile updateProfile(Profile profile) {
        checkUserAndApplication(profile);
        return getProfileDao().updateActiveProfile(profile);
    }

    @Override
    public Profile createProfile(Profile profile) {
        checkUserAndApplication(profile);
        return getProfileDao().createOrReactivateProfile(profile);
    }

    private void checkUserAndApplication(final Profile requestedProfile) {

        if (!Objects.equals(getUser(), requestedProfile.getUser())) {
            throw new InvalidDataException("Profile user must match current user.");
        }

    }

    @Override
    public void deleteProfile(String profileId) {

        if (!Objects.equals(getCurrentProfile().getId(), profileId)) {
            throw new NotFoundException();
        }

        getProfileDao().softDeleteProfile(profileId);

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public UserService getUserService() {
        return userService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

}
