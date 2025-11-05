package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
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
     * Blacklists the {@link Session} instance currently in-use for the specific user id as returned by
     * {@link User#getId()}
     *
     * @param sessionSecret the session secret
     */
    void blacklistSession(String sessionSecret);

}
