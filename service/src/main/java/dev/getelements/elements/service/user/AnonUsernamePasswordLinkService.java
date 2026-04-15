package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.user.UsernamePasswordLinkService;

/**
 * Anonymous implementation of {@link UsernamePasswordLinkService}.
 *
 * <p>Always throws {@link ForbiddenException}; authentication is required to link credentials.
 */
public class AnonUsernamePasswordLinkService implements UsernamePasswordLinkService {

    @Override
    public User linkUsernamePassword(final String username, final String password) {
        throw new ForbiddenException("Authentication required to link credentials.");
    }

}
