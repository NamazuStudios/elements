package dev.getelements.elements.dao;

import dev.getelements.elements.model.session.GoogleSignInSession;
import dev.getelements.elements.model.session.GoogleSignInSessionCreation;
import dev.getelements.elements.model.session.Session;

import java.util.Optional;

/**
 * Used to manage the instances of {@link Session} created using the Google sign-in process. This process.
 */
public interface GoogleSignInSessionDao {

    /**
     * Creates s {@link Session} with the
     *
     * @param session the {@link Session} to create
     * @return
     */
    GoogleSignInSessionCreation create(Session session);

    /**
     * Finds an instance of {@link GoogleSignInSession} using the session secret. Since not all sessions are created with
     * the Google Sign-In process, this may find no results even if the supplied secret is a valid session. Therefore,
     * this method return an {@link Optional<GoogleSignInSession>} instance.
     *
     * This can be used to re-verify the session with the refresh service.
     *
     * @param sessionSecret the session secret
     * @return an {@link Optional<GoogleSignInSession>} instance
     */
    Optional<GoogleSignInSession> findSession(String sessionSecret);

}
