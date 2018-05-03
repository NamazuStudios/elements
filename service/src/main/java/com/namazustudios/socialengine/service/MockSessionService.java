package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.MockSessionCreation;
import com.namazustudios.socialengine.model.session.MockSessionRequest;
import com.namazustudios.socialengine.model.session.Session;

/**
 * Manages instances of mock session.  A mock session creates a {@link User} and {@link Profile} to go with the
 * {@link Session}.  Optionally, the mock account will be deleted at a later time.  This is intended to be used for
 * testing and development.
 */
public interface MockSessionService {

    /**
     * Creates a {@link Session}, {@link User}, and {@link Profile}.  The created {@link User} is assigned a scrambled
     * password which is returned int he {@link MockSessionCreation}.
     *
     * @param mockSessionRequest the {@link MockSessionRequest}
     * @return the {@link MockSessionCreation}
     */
    MockSessionCreation createMockSession(MockSessionRequest mockSessionRequest);

}
