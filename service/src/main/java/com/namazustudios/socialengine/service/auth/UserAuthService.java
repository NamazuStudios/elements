package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.AuthService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 4/2/15.
 */
public class UserAuthService implements AuthService {

    @Inject
    private User user;

    @Inject
    private Session session;

    @Override
    public Session getSession(String sessionId) {
        return null;
    }

    @Override
    public Session createSessionWithLogin(String userId, String password) {
        return null;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Session getSession() {
        return session;
    }

    @Inject
    public void setSession(Session session) {
        this.session = session;
    }

}
