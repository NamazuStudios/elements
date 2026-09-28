package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/** SUPERUSER-only administrative operations on other accounts' TOTP enrollment. */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface TotpAdminService {

    /**
     * Disables and clears TOTP enrollment for the given user's account -- the account-recovery path for a
     * lost authenticator device with no remaining recovery codes.
     *
     * @param userId the ID of the user whose enrollment should be reset
     */
    void resetTotp(String userId);

}
