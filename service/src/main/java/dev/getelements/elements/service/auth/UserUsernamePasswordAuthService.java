package dev.getelements.elements.service.auth;

import dev.getelements.elements.dao.SessionDao;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.rt.exception.BadRequestException;
import dev.getelements.elements.service.UsernamePasswordAuthService;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Created by patricktwohig on 4/2/15.
 */
public class UserUsernamePasswordAuthService implements UsernamePasswordAuthService {

    private Session session;

    public Session getSession() {
        return session;
    }

    @Inject
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public SessionCreation createSession(UsernamePasswordSessionRequest usernamePasswordSessionRequest) {
        throw new BadRequestException("Session already active.");
    }

}
