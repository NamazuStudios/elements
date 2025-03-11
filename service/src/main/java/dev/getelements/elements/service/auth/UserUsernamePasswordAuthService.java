package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.rt.exception.BadRequestException;

import dev.getelements.elements.sdk.service.auth.UsernamePasswordAuthService;
import jakarta.inject.Inject;

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
