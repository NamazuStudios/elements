package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.session.Session;

public interface SessionService {

    /**
     * Finds an instance of {@link Session} based on the id, as determiend by {@link Session#getId()}.
     *
     * @param sessionId the {@link Session} identifier
     *
     * @return the {@link Session}, never null.  Throws the appropriate exception if session isn't found.
     *
     */
    Session checkSession(String sessionId);

    /**
     * Destroys all sessions.
     */
    void destroySessions();

    /**
     * Destroys the {@link Session} instance currently in-use.
     * @param session
     */
    void destroySession(String session);

}
