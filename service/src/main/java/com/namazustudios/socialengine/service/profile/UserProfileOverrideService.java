package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.ProfileOverrideService;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Implementation for users with {@link User.Level#USER} users.
 */
public class UserProfileOverrideService implements ProfileOverrideService {

    private User user;

    private ProfileDao profileDao;

    /**
     * This checks for hte profile ID and additionally ensures that the currently authenticated user owns the
     * profile.  If a mismatch occurrs, then this will fail silently and simply return a null value.  This is intended
     * to make is such that the server will neither confirm nor deny that the profile exists or is assocaited with
     * the particular user.
     *
     * @param profileId the profile ID
     * @return the profile ID
     */
    @Override
    public Optional<Profile> findOverrideProfile(final String profileId) {
        return getProfileDao().findActiveProfileForUser(profileId, getUser().getId());
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
