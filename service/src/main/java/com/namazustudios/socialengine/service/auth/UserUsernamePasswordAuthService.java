package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.service.UsernamePasswordAuthService;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Created by patricktwohig on 4/2/15.
 */
public class UserUsernamePasswordAuthService implements UsernamePasswordAuthService {

    private Session session;

    @Override
    public SessionCreation createSessionWithLogin(final String userId, final String password) {
        throw new BadRequestException("Session already active.");
    }

    @Override
    public SessionCreation createSessionWithLogin(final String userId, final String password, final String profileId) {
        throw new BadRequestException("Session already active.");
    }

    public Session getSession() {
        return session;
    }

    @Inject
    public void setSession(Session session) {
        this.session = session;
    }

}
