package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.MockSessionCreation;
import dev.getelements.elements.sdk.model.session.MockSessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages instances of mock session.  A mock session creates a {@link User} and {@link Profile} to go with the
 * {@link Session}.  Optionally, the mock account may be deleted at a later time.  This is intended to be used for
 * testing and development.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
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
