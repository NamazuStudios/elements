package dev.getelements.elements.service;

import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.security.BasicAuthorizationHeader;

/**
 * Created by patricktwohig on 4/1/15.
 */
public interface UsernamePasswordAuthService {

    /**
     * Performs a sign-in with the supplied {@link UsernamePasswordSessionRequest}.
     *
     * @param basicAuthorizationHeader the basic authorization header
     *
     * @return the {@link SessionCreation} if the session was created successfully.
     */
    default SessionCreation createSession(BasicAuthorizationHeader basicAuthorizationHeader) {
        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(basicAuthorizationHeader.getUsername());
        request.setPassword(basicAuthorizationHeader.getPassword());
        return createSession(request);
    }

    /**
     * Performs a sign-in with the supplied {@link UsernamePasswordSessionRequest}.
     *
     * @param usernamePasswordSessionRequest the user name password session request
     *
     * @return the {@link SessionCreation} if the session was created successfully.
     */
    SessionCreation createSession(UsernamePasswordSessionRequest usernamePasswordSessionRequest);

}
