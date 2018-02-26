package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.session.Session;

/**
 * Manages instances of {@link Session}.
 */
public interface SessionDao {

    /**
     * Gets the {@link Session} by it's session id, as returned by {@link Session#getId()}.  Throwing an appropriate
     * exception type if the {@link Session} can't be found.
     *
     * @param sessionId the session's ID
     * @return the {@link Session} never null
     *
     */
    Session getBySessionId(String sessionId);

    /**
     * Creates a {@link Session} with the provided {@link Session} object.  This will include all information related
     * to the {@link Session}.
     *
     * @param session
     * @return the {@link Session} as it was created in the database
     */
    Session create(Session session);

    /**
     * Deletes the {@link Session} instance.  The id is determined by {@link Session#getId()}
     * @param sessionId
     */
    void delete(String sessionId);

    /**
     * Delete the {@link Session} instance.
     *
     * @param session
     */
    default void delete(final Session session) {
        delete(session.getId());
    }

}
