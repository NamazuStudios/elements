package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.SessionService;

import javax.inject.Inject;

public class AnonSessionService implements SessionService {

    private SessionDao sessionDao;

    @Override
    public Session checkSession(final String sessionSecret) {
        try {
            return getSessionDao().getBySessionSecret(sessionSecret);
        } catch (NotFoundException ex) {
            throw new ForbiddenException(ex);
        }
    }

    @Override
    public void destroySessions() {
        throw new ForbiddenException();
    }

    @Override
    public void destroySession(String session) {
        throw new ForbiddenException();
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

}
