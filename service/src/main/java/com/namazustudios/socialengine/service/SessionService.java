package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;

public interface SessionService {

    /**
     * Finds an instance of {@link Session} based on the id, as determined by
     * {@link SessionCreation#getSessionSecret()}.  In addition to performing a check for a valid {@link Session}, this
     * will reset the expiry of the {@link Session}.
     *
     * @param sessionSecret the {@link Session} identifier
     *
     * @return the {@link Session}, never null.  Throws the appropriate exception if session isn't found.
     *
     */
    Session checkAndRefreshSessionIfNecessary(String sessionSecret);

    /**
     * Destroys all sessions for the given ID as returned by {@link User#getId()}
     * @param userId the user Id
     */
    void destroySessions(final String userId);

    /**
     * Destroys the {@link Session} instance currently in-use for the specific user id as returned by
     * {@link User#getId()}
     *
     * @param userId the user Id
     * @param sessionSecret the session secret
     */
    void destroySession(final String userId, String sessionSecret);

}
