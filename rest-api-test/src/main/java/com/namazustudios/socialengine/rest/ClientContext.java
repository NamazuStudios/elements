package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static com.namazustudios.socialengine.model.User.Level.USER;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

class ClientContext {

    public static final String CONTEXT_APPLICATION = "com.namazustudios.socialengine.rest.test.client.context.application";

    private UserDao userDao;

    private ProfileDao profileDao;

    private SessionDao sessionDao;

    private User user;

    private List<Profile> profiles = new ArrayList<>();

    private Application application;

    private SessionCreation sessionCreation;

    public ClientContext newTestClientContext(final String userName, int profileCount) {

        createUser(userName);

        for (int i = 0; i< profileCount; ++i) createProfile(format("%s profile %d", userName, i));

        if (profileCount <= 0) {
            sessionCreation = createSession();
        } else {
            sessionCreation = createSession(getDefaultProfile());
        }

        return this;

    }

    public User getUser() {
        return user;
    }

    public Profile getDefaultProfile() {
        if (profiles.isEmpty()) throw new IllegalStateException("No profiles set.");
        return profiles.get(0);
    }


    public String getSessionSecret() {
        if (sessionCreation == null) throw new IllegalStateException("No profiles set.");
        return sessionCreation.getSessionSecret();
    }

    public User createUser(final String name) {
        try {
            final User user = new User();
            user.setName(name);
            user.setEmail(format("%s@example.com", name));
            user.setLevel(USER);
            user.setActive(true);
            return this.user = userDao.createUserWithPasswordStrict(user, "password");
        } finally {
            profiles.clear();
            sessionCreation = null;
        }
    }

    public Profile createProfile(final String display) {
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setDisplayName(display);
        profile.setApplication(application);

        profile = profileDao.createOrReactivateProfile(profile);
        profiles.add(profile);
        return profile;
    }

    public SessionCreation createSession() {
        final Session session = new Session();
        final long expiry = MILLISECONDS.convert(1, DAYS) + currentTimeMillis();
        session.setUser(user);
        session.setExpiry(expiry);
        session.setApplication(application);
        return sessionCreation = sessionDao.create(session);
    }

    public SessionCreation createSession(final Profile profile) {
        final Session session = new Session();
        final long expiry = MILLISECONDS.convert(1, DAYS) + currentTimeMillis();
        session.setUser(user);
        session.setExpiry(expiry);
        session.setProfile(profile);
        session.setApplication(application);
        return sessionCreation = sessionDao.create(session);
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
}
