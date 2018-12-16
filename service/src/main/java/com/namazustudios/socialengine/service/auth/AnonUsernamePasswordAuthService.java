package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.service.UsernamePasswordAuthService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.util.Objects;

import static com.namazustudios.socialengine.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by patricktwohig on 4/1/15.
 */
@Singleton
public class AnonUsernamePasswordAuthService implements UsernamePasswordAuthService {

    private UserDao userDao;

    private SessionDao sessionDao;

    private ProfileDao profileDao;

    private long sessionTimeoutSeconds;

    @Override
    public SessionCreation createSessionWithLogin(final String userId, final String password) {

        final User user = getUserDao().validateActiveUserPassword(userId, password);

        final Session session = new Session();
        session.setUser(user);

        final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();
        session.setExpiry(expiry);

        return getSessionDao().create(session);

    }

    @Override
    public SessionCreation createSessionWithLogin(final String userId, final String password, final String profileId) {

        final User user = getUserDao().validateActiveUserPassword(userId, password);
        final Profile profile = getProfileDao().getActiveProfile(profileId);

        if (!Objects.equals(user, profile.getUser())) {
            throw new ForbiddenException("Invalid credentials for " + userId);
        }

        final Session session = new Session();

        session.setUser(user);
        session.setProfile(profile);

        final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();
        session.setExpiry(expiry);



        return getSessionDao().create(session);

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Inject
    public void setSessionTimeoutSeconds(@Named(SESSION_TIMEOUT_SECONDS) long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

}
