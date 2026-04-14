package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.service.auth.OidcLinkService;

/**
 * Anonymous implementation of {@link OidcLinkService}.
 *
 * <p>Always throws {@link ForbiddenException}; authentication is required to link accounts.
 */
public class AnonOidcLinkService implements OidcLinkService {

    @Override
    public SessionCreation createSession(final OidcSessionRequest oidcSessionRequest) {
        throw new ForbiddenException("Authentication required to link accounts.");
    }

}
