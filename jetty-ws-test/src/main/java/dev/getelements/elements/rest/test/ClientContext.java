package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.service.profile.ProfileImageObjectUtils;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ClientContext {

    public static final String DUMMY_PASSWORD = "password";

    public static final String CONTEXT_APPLICATION = "dev.getelements.elements.rest.test.client.context.application";

    private static final AtomicInteger userNameDecorator = new AtomicInteger();

    private UserDao userDao;

    private ProfileDao profileDao;

    private SessionDao sessionDao;

    private User user;

    private List<Profile> profiles = new ArrayList<>();

    private Application application;

    private SessionCreation sessionCreation;

    private ProfileImageObjectUtils profileImageObjectUtils;

    private LargeObjectDao largeObjectDao;

    public User getUser() {
        return user;
    }

    public Profile getDefaultProfile() {
        if (profiles.isEmpty()) throw new IllegalStateException("No profiles set.");
        return profiles.get(0);
    }

    public String getSessionSecret() {
        if (sessionCreation == null) throw new IllegalStateException("No session secret set.");
        return sessionCreation.getSessionSecret();
    }

    public ClientContext createUser(final String name) {
        try {
            final var user = new User();
            final var decoratedName = format("%s.%05d", name, userNameDecorator.getAndIncrement());
            user.setName(decoratedName);
            user.setEmail(format("%s@example.com", decoratedName));
            user.setLevel(USER);
            this.user = userDao.createUserWithPasswordStrict(user, DUMMY_PASSWORD);
            return this;
        } finally {
            profiles.clear();
            sessionCreation = null;
        }
    }

    public ClientContext createSuperuser(final String name) {
        try {
            final var user = new User();
            final var decoratedName = format("%s.%05d", name, userNameDecorator.getAndIncrement());
            user.setName(decoratedName);
            user.setEmail(format("%s@example.com", decoratedName));
            user.setLevel(SUPERUSER);
            this.user = userDao.createUserWithPasswordStrict(user, DUMMY_PASSWORD);
            return this;
        } finally {
            profiles.clear();
            sessionCreation = null;
        }
    }

    public ClientContext createProfiles(int count) {
        for (int i = 0; i< count; ++i) createProfile(format("%s profile %d", count, i));
        return this;
    }

    public ClientContext createProfile(final String display) {
        final Profile profile = new Profile();
        profile.setUser(user);
        profile.setDisplayName(display);
        profile.setApplication(application);

        profiles.add(profileDao.createOrReactivateProfile(profile));

        Profile createdProfile = getDefaultProfile();
        LargeObjectReference imageObjectReference = createImageObjectForProfile(createdProfile);
        createdProfile.setImageObject(imageObjectReference);

        profiles.set(0, profileDao.updateActiveProfile(createdProfile));

        return this;
    }

    public ClientContext createSession() {
        return createSession(null);
    }

    public ClientContext createSessionWithDefaultProfile() {
        final var profile = getDefaultProfile();
        return createSession(profile);
    }

    public ClientContext createSession(final Profile profile) {

        final Session session = new Session();
        final long expiry = MILLISECONDS.convert(1, DAYS) + currentTimeMillis();

        session.setUser(user);
        session.setExpiry(expiry);

        if (profile != null) {
            session.setProfile(profile);
            session.setApplication(profile.getApplication());
        }

        sessionCreation = sessionDao.create(session);
        return this;

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

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public Application getApplication() {
        return application;
    }

    @Inject
    public void setApplication(@Named(CONTEXT_APPLICATION) Application application) {
        this.application = application;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public SessionCreation getSessionCreation() {
        return sessionCreation;
    }

    @Inject
    public void setLargeObjectDao(LargeObjectDao largeObjectDao) {
        this.largeObjectDao = largeObjectDao;
    }

    @Inject
    public void setProfileImageObjectUtils(ProfileImageObjectUtils profileImageObjectUtils) {
        this.profileImageObjectUtils = profileImageObjectUtils;
    }

    public ProfileImageObjectUtils getProfileImageObjectUtils() {
        return profileImageObjectUtils;
    }

    public LargeObjectDao getLargeObjectDao() {
        return largeObjectDao;
    }

    private LargeObjectReference createImageObjectForProfile(Profile profile) {
        LargeObject imageObject = getProfileImageObjectUtils().createImageObject(profile);
        LargeObject persistedObject = getLargeObjectDao().createLargeObject(imageObject);
        return getProfileImageObjectUtils().createReference(persistedObject);
    }

}
