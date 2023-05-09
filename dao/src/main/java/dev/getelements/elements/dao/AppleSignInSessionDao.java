package dev.getelements.elements.dao;

import dev.getelements.elements.model.applesignin.TokenResponse;
import dev.getelements.elements.model.session.AppleSignInSession;
import dev.getelements.elements.model.session.AppleSignInSessionCreation;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.session.SessionCreation;

import java.util.Optional;

/**
 * Used to manage the instances of {@link Session} created using the Apple sign-in process. This process.
 */
public interface AppleSignInSessionDao {

    /**
     * Creates s {@link Session} with the
     *
     * @param session the {@link Session} to create
     * @param tokenResponse
     * @return
     */
    AppleSignInSessionCreation create(Session session, TokenResponse tokenResponse);

    /**
     * Finds an instance of {@link AppleSignInSession} using the session secret. Since not all sessions are created with
     * the Apple Sign-In process, this may find no results even if the supplied secret is a valid session. Therefore,
     * this method return an {@link Optional<AppleSignInSession>} instance.
     *
     * This can be used to re-verify the session with the refresh service.
     *
     * @param sessionSecret the session secret
     * @return an {@link Optional<AppleSignInSession>} instance
     */
    Optional<AppleSignInSession> findSession(String sessionSecret);

}
