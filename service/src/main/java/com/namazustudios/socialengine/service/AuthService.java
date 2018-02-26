package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.Session;

/**
 * Created by patricktwohig on 4/1/15.
 */
public interface AuthService {

    /**
     * Generates an API key for the given userId and password.
     *
     * @param userId the user Id
     * @param password the user's password
     * @return the API key instance
     * @deprecated user {@link #createSessionWithLogin(String, String)}
     */
    default User loginUser(final String userId, final String password) {
        final Session session = createSessionWithLogin(userId, password);
        return session.getUser();
    }

    /**
     * Finds an instance of {@link Session} based on the id, as determiend by {@link Session#getId()}.
     *
     * @param sessionId the {@link Session} identifier
     *
     * @return the {@link Session}, never null.  Throws the appropriate exception if session isn't found.
     *
     */
    Session getSession(String sessionId);

    /**
     * Creates a {@link Session} with the login credential for a {@link User}.
     *
     * @param userId
     * @param password
     * @return
     */
    Session createSessionWithLogin(final String userId, final String password);

    /**
     * Destroys the {@link Session} instance currently in-use.
     */
    void destroyCurrentSession();

}
