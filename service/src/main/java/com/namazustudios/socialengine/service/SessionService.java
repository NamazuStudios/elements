package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;

public interface SessionService {

    /**
     * Finds an instance of {@link Session} based on the id, as determined by
     * {@link SessionCreation#getSessionSecret()}.
     *
     * @param sessionSecret the {@link Session} identifier
     *
     * @return the {@link Session}, never null.  Throws the appropriate exception if session isn't found.
     *
     */
    Session checkSession(String sessionSecret);

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
