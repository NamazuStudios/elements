package com.namazustudios.socialengine.service.util;

import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.ConflictException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;
import java.util.Objects;

public class UserProfileUtility {

    private UserDao userDao;

    private ProfileDao profileDao;

    public UsernameProfileRecord getAndCheckForMatch(final String userId, final String profileId) {

        if (userId == null && profileId == null) {
            throw new InvalidDataException("Must specify either user or profile.");
        }

        var user = userId == null ? null : getUserDao().getActiveUser(userId);
        var profile = profileId == null ? null : getProfileDao().getActiveProfile(profileId);

        if (user != null && profile != null) {
            if (Objects.equals(user.getId(), profile.getUser().getId()))
                throw new ConflictException("User and profile do not match.");
            return new UsernameProfileRecord(user, profile);
        } else if (profile != null) {
            return new UsernameProfileRecord(profile.getUser(), profile);
        } else if (user != null) {
            return new UsernameProfileRecord(user, null);
        } else {
            throw new InvalidDataException("Must specify either user or profile.");
        }

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public static class UsernameProfileRecord {

        public final User user;

        public final Profile profile;

        public UsernameProfileRecord(final User user, final Profile profile) {
            this.user = user;
            this.profile = profile;
        }

    }

}
