package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.session.Session;

/**
 * Used to authorize requests using JWT auth tokens
 *
 * Created by robb on 12/20/21.
 */
public interface CustomAuthSessionService {

    /**
     * Gets a {@link Session} provided the custom auth JWT.
     *
     * @param jwt the jwt
     * @return the the {@link Session} instance
     */
    Session getSession(final String jwt);

}
