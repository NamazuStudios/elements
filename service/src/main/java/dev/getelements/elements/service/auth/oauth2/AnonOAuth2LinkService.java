package dev.getelements.elements.service.auth.oauth2;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.service.auth.OAuth2LinkService;

/**
 * Anonymous implementation of {@link OAuth2LinkService}.
 *
 * <p>Always throws {@link ForbiddenException}; authentication is required to link accounts.
 */
public class AnonOAuth2LinkService implements OAuth2LinkService {

    @Override
    public SessionCreation createSession(final OAuth2SessionRequest oAuth2SessionRequest) {
        throw new ForbiddenException("Authentication required to link accounts.");
    }

}
