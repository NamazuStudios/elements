package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;

/**
 * Dedicated service interface for the account-linking OIDC flow
 * ({@code POST /user/me/link/oidc}).
 *
 * <p>Kept as a standalone type (not extending {@link OidcAuthService}) so that the
 * Guice-HK2 bridge can resolve it without type-hierarchy ambiguity.
 */
@ElementPublic
@ElementServiceExport
public interface OidcLinkService {

    /**
     * Links an OIDC identity to the currently authenticated user's account.
     *
     * @param request the OIDC session request containing the external token
     * @return the resulting {@link SessionCreation}
     */
    SessionCreation createSession(OidcSessionRequest request);

}
