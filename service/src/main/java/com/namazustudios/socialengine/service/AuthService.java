package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.User;

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
     */
    User loginUser(final String userId, final String password);

}

