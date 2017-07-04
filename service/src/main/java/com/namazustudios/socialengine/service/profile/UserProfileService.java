package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.ProfileService;

import javax.inject.Inject;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class UserProfileService implements ProfileService {

    private User user;

    private ProfileDao profileDao;

    private Supplier<Profile> currentProfileSupplier;

    @Override
    public Pagination<Profile> getProfiles(int offset, int count) {
        return getProfileDao().getActiveProfiles(offset, count);
    }

    @Override
    public Pagination<Profile> getProfiles(int offset, int count, String search) {
        return getProfileDao().getActiveProfiles(offset, count, search);
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
        return getProfileDao().createOrReactivateProfile(profile);
    }

    @Override
    public Profile createProfile(Profile profile) {
        try {
            final Profile currentProfile = getCurrentProfile();
            throw new DuplicateException("profile already exists for user " + currentProfile.getUser().getId());
        } catch (NotFoundException ex) {
            checkUserAndApplication(profile);
            return getProfileDao().createOrReactivateProfile(profile);
        }
    }

    private void checkUserAndApplication(final Profile requestedProfile) {

        if (!Objects.equals(getUser(), requestedProfile.getUser())) {
            throw new InvalidDataException("Profile user must match current user.");
        }

        if (!Objects.equals(getCurrentProfile().getApplication(), requestedProfile.getApplication())) {
            throw new InvalidDataException("Profile application must match current profile application.");
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
