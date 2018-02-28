package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;

/**
 * Manages instances of {@link Session}.
 */
public interface SessionDao {

    /**
     * Gets the {@link Session} by it's session id, as returned by {@link SessionCreation#getSessionSecret()} ()}.
     * Throwing an appropriate exception type if the {@link Session} can't be found.
     *
     * @param sessionSecret the session's ID
     * @return the {@link Session} never null
     *
     */
    Session getBySessionSecret(String sessionSecret);

    /**
     * Creates a {@link Session} with the provided {@link Session} object.  This will return an instance of
     * {@link SessionCreation} providing a secret key which can be used to access the {@link Session} in the future.
     *
     *
     * @param user
     * @param session
     * @return the {@link SessionCreation} as it was created in the database
     */
    SessionCreation create(User user, Session session);

    /**
     * Deletes the {@link Session} instance.  The secret is determined by {@link SessionCreation#getSessionSecret()}.
     *
     * @param sessionSecret
     */
    void delete(String sessionSecret);

    /**
     * Deletes all instances of {@link Session} for the provided {@link User}.
     *
     * @param userId the {@link User} id, as determinted by {@link User#getId()}.
     */
    void deleteAllSessionsForUser(String userId);

}
