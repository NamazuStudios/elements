package dev.getelements.elements.sdk.service.user;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.user.User;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface UsernamePasswordLinkService {

    /**
     * Links username+password credentials to the authenticated user's account.
     *
     * <p>If the account has no username yet, the supplied username is claimed and recorded.
     * If the account already has a username it must match the supplied value; name changes
     * must be performed explicitly via the user-update endpoints.
     *
     * @param username the username to associate (must be available or already owned by this user)
     * @param password the password to set
     * @return the updated {@link User}
     */
    User linkUsernamePassword(String username, String password);

}
