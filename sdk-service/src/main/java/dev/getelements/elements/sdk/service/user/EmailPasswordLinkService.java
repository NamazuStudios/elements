package dev.getelements.elements.sdk.service.user;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.user.User;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface EmailPasswordLinkService {

    /**
     * Links email+password credentials to the authenticated user's account.
     * The email UID must already be VERIFIED through the email-verification flow.
     * Returns the updated User.
     *
     * @param email    the verified email address to link
     * @param password the password to associate with this email
     * @return the updated {@link User}
     */
    User linkEmailPassword(String email, String password);

}
