package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.Session;

/**
 * Created by patricktwohig on 4/1/15.
 */
public interface UsernamePasswordAuthService {

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
     * Creates a {@link Session} with the login credential for a {@link User}.
     *
     * @param userId the user ID
     * @param password the password
     * @return the {@link Session} created
     */
    Session createSessionWithLogin(String userId, String password);

    /**
     * Destroys the {@link Session} instance currently in-use.
     */
    void destroyCurrentSession();

}
