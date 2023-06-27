package dev.getelements.elements.service.auth;

import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.SessionDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.service.UsernamePasswordAuthService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.util.Objects;

import static dev.getelements.elements.Constants.SESSION_TIMEOUT_SECONDS;
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

        final Application application = profile.getApplication();

        if (application != null) {
            session.setApplication(application);
        }

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
