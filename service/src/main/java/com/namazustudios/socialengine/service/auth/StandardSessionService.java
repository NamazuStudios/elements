package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.SessionService;

import javax.inject.Inject;

public class StandardSessionService implements SessionService {

    private SessionDao sessionDao;

    @Override
    public Session checkSession(String sessionId) {
        try {
            return getSessionDao().getBySessionId(sessionId);
        } catch (NotFoundException ex) {
            throw new ForbiddenException(ex);
        }
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

}
