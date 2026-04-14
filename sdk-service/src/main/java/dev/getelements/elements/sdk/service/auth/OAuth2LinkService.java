package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;

/**
 * Dedicated service interface for the account-linking OAuth2 flow
 * ({@code POST /user/me/link/oauth2}).
 *
 * <p>Kept as a standalone type (not extending {@link OAuth2AuthService}) so that the
 * Guice-HK2 bridge can resolve it without type-hierarchy ambiguity.
 */
@ElementPublic
@ElementServiceExport
public interface OAuth2LinkService {

    /**
     * Links an OAuth2 identity to the currently authenticated user's account.
     *
     * @param request the OAuth2 session request containing the external token
     * @return the resulting {@link SessionCreation}
     */
    SessionCreation createSession(OAuth2SessionRequest request);

}
