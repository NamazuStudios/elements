package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.session.Session;

import dev.getelements.elements.sdk.service.auth.SessionService;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DefaultSessionService implements SessionService {

    private SessionDao sessionDao;

    private long sessionTimeoutSeconds;

    @Override
    public Session checkAndRefreshSessionIfNecessary(final String sessionSecret) {

        final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();

        try {
            return getSessionDao().refresh(sessionSecret, expiry);
        } catch (NotFoundException ex) {
            throw new ForbiddenException(ex);
        }

    }

    @Override
    public void blacklistSession(final String sessionSecret) {
        getSessionDao().blacklist(sessionSecret);
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Inject
    public void setSessionTimeoutSeconds(@Named(SESSION_TIMEOUT_SECONDS) long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

}
