package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.ProfileOverrideService;

import javax.inject.Inject;

/**
 * Implementation for users with {@link User.Level#SUPERUSER} users.
 */
public class SuperUserProfileOverrideService implements ProfileOverrideService {

    private ProfileDao profileDao;

    /**
     * Returns the profile, regardless of ownership.
     *
     * @param profileId the profile ID
     * @return the profile or null, if it doesn't exist.
     */
    @Override
    public Profile findOverrideProfile(String profileId) {
        return getProfileDao().findActiveProfile(profileId);
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

}
