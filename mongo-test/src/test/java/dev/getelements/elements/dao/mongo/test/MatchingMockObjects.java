package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.profile.Profile;

import jakarta.inject.Inject;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public class MatchingMockObjects {

    private UserDao userDao;

    private ProfileDao profileDao;

    private ApplicationDao applicationDao;

    private UserTestFactory userTestFactory;

    private static final AtomicInteger applicationCount = new AtomicInteger();

    public Application makeMockApplication() {
        final Application application = new Application();
        application.setName(format("match_%d", applicationCount.getAndIncrement()));
        application.setDescription("A mock application for matchmaking tests.");
        return getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    public User makeMockUser() {
        return getUserTestFactory().createTestUser();
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

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

}
