package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.service.UsernamePasswordAuthService;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Created by patricktwohig on 4/2/15.
 */
public class UserUsernamePasswordAuthService implements UsernamePasswordAuthService {

    private Session session;

    private SessionDao sessionDao;

    @Override
    public Session createSessionWithLogin(String userId, String password) {
        throw new BadRequestException();
    }

    @Override
    public void destroyCurrentSession() {
        getSessionDao().delete(getSession());
    }

    public Session getSession() {
        return session;
    }

    @Inject
    public void setSession(Session session) {
        this.session = session;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

}
