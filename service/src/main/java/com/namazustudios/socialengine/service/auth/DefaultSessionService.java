package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.SessionService;

import javax.inject.Inject;
import javax.inject.Named;

import static com.namazustudios.socialengine.Constants.SESSION_TIMEOUT_SECONDS;
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
    public void destroySessions(final String userId) {
        getSessionDao().deleteAllSessionsForUser(userId);
    }

    @Override
    public void destroySession(final String userId, final String sessionSecret) {
        getSessionDao().delete(userId, sessionSecret);
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
