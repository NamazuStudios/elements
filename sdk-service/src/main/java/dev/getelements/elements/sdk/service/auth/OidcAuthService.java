package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface OidcAuthService {

    /**
     * Performs a sign-in with the supplied {@link OidcSessionRequest}.
     *
     * @param oidcSessionRequest the oidc session request object
     *
     * @return the {@link SessionCreation} if the session was created successfully.
     */
    SessionCreation createSession(OidcSessionRequest oidcSessionRequest);

}
