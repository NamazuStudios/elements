package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.ProfileService;

import javax.inject.Inject;
import java.util.function.Supplier;

/**
 * Provides full access to the {@link Profile} and related types.  Should be
 * used in conjunction with users SUPERUSER level access.
 *
 * Created by patricktwohig on 6/28/17.
 */
public class SuperUserProfileService implements ProfileService {

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
        return getProfileDao().updateActiveProfile(profile, profile.getMetadata());
    }

    @Override
    public Profile createProfile(Profile profile) {
        return getProfileDao().createOrReactivateProfile(profile, profile.getMetadata());
    }

    @Override
    public void deleteProfile(String profileId) {
        getProfileDao().softDeleteProfile(profileId);
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
