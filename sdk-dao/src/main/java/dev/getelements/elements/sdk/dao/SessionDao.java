package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Manages instances of {@link Session}.
 */
@ElementServiceExport
public interface SessionDao {

    /**
     * Gets the {@link Session} by it's session id, as returned by {@link SessionCreation#getSessionSecret()} ()}.
     * Throwing an appropriate exception type if the {@link Session} can't be found.
     *
     * @param sessionSecret the session's ID
     * @return the {@link Session} never null
     */
    Session getBySessionSecret(String sessionSecret);

    /**
     * Refreshs the {@link Session} with the supplied expiry.
     *
     * @param sessionSecret the session secret key
     * @param expiry        the expiry timestamp, as expression milliseconds since the Unix epoch
     * @return the updated {@link Session}
     */
    Session refresh(String sessionSecret, long expiry);

    /**
     * Creates a {@link Session} with the provided {@link Session} object.  This will return an instance of
     * {@link SessionCreation} providing a secret key which can be used to access the {@link Session} in the future.
     *
     * @param session
     * @return the {@link SessionCreation} as it was created in the database
     */
    SessionCreation create(Session session);

    /**
     * Deletes the {@link Session} instance.  The secret is determined by {@link SessionCreation#getSessionSecret()}.
     *
     * @param sessionSecret the session secret
     */
    void blacklist(String sessionSecret);

}
