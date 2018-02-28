package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.SessionService;

import javax.inject.Inject;
import java.util.Objects;

public class UserSessionService implements SessionService {

    private User user;

    private SessionDao sessionDao;

    @Override
    public Session checkSession(final String sessionSecret) {

        final Session session = getSessionDao().getBySessionId(sessionSecret);

        if (Objects.equals(getUser(), session.getUser())) {
            return session;
        } else {
            throw new ForbiddenException();
        }

    }

    @Override
    public void destroySessions() {
        getSessionDao().deleteAllSessionsForUser(getUser().getId());
    }

    @Override
    public void destroySession(final String sessionId) {
        getSessionDao().deleteSessionForUser(sessionId, getUser().getId());
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

}

