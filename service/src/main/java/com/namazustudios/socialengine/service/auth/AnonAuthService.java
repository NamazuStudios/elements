package com.namazustudios.socialengine.service.auth;

import com.mchange.rmi.NotAuthorizedException;
import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.AuthService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by patricktwohig on 4/1/15.
 */
@Singleton
public class AnonAuthService implements AuthService  {

    private UserDao userDao;

    private SessionDao sessionDao;

    @Override
    public Session getSession(final String sessionId) {
        return getSessionDao().getBySessionId(sessionId);
    }

    @Override
    public Session createSessionWithLogin(final String userId, final String password) {

        final User user = getUserDao().validateActiveUserPassword(userId, password);

        final Session session = new Session();
        session.setUser(user);

        return getSessionDao().create(session);

    }

    @Override
    public void destroyCurrentSession() {
        throw new ForbiddenException();
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

}
