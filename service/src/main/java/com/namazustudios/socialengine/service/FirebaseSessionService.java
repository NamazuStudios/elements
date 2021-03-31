package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.session.Session;

import java.util.Optional;

/**
 * Drives support for Firebase backed {@link Session} instances.
 */
public interface FirebaseSessionService {

    /**
     * Attempts to verify the supplied session secret supplied directly by Firebase. This should be a JWT token which
     * the service will attempt to verify. If successful, the returned {@link Session} will attempt to match against the
     * project identifier within Firebase.
     *
     * If verification fails, such as passing an expired Firebase JWT token this will throw an instance of
     * {@link ForbiddenException}. However if the token is not parseable, or this service can detect it is not a JWT
     * token for Firebase this will fail quietly as to allow subsequent means to verify the token.
     *
     * @param sessionSecret the session secret
     * @return an {@link Optional<Session>} which will contain a valid session if applicable.
     */
    Optional<Session> attemptVerification(String sessionSecret);

}
