package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.user.EmailPasswordLinkService;

/**
 * Anonymous implementation of {@link EmailPasswordLinkService}.
 *
 * <p>Always throws {@link ForbiddenException}; authentication is required to link credentials.
 */
public class AnonEmailPasswordLinkService implements EmailPasswordLinkService {

    @Override
    public User linkEmailPassword(final String email, final String password) {
        throw new ForbiddenException("Authentication required to link credentials.");
    }

}
