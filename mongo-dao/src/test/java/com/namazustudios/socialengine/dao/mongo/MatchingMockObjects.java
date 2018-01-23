package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.profile.Profile;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.User.Level.USER;
import static java.lang.String.format;

public class MatchingMockObjects {

    private UserDao userDao;

    private ProfileDao profileDao;

    private ApplicationDao applicationDao;

    public Application makeMockApplication() {
        final Application application = new Application();
        application.setName("mock");
        application.setDescription("A mock application.");
        return getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    public User makeMockUser(final String name) {
        final User user = new User();
        user.setName(name);
        user.setEmail(format("%s@example.com", name));
        user.setLevel(USER);
        return getUserDao().createOrReactivateUser(user);
    }

    public Profile makeMockProfile(final User user, final Application application) {
        final Profile profile =  new Profile();
        profile.setUser(user);
        profile.setApplication(application);
        profile.setDisplayName(format("display-name-%s", user.getName()));
        profile.setImageUrl(format("http://example.com/%s.png", user.getName()));
        return getProfileDao().createOrReactivateProfile(profile);
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

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

}
