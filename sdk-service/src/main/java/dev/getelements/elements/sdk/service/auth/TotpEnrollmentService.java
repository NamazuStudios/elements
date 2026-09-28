package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.TotpEnrollment;
import dev.getelements.elements.sdk.model.auth.TotpRecoveryCodes;

/**
 * Self-service TOTP enrollment for the calling user's own account. Available to any authenticated account
 * (USER or SUPERUSER) -- TOTP is an optional, per-account security feature, not restricted to admins.
 */
@ElementPublic
@ElementServiceExport
public interface TotpEnrollmentService {

    /**
     * Begins (or restarts) enrollment for the calling user: generates a new shared secret, not yet enforced
     * at login until confirmed with a valid code.
     *
     * @return the pending {@link TotpEnrollment}
     */
    TotpEnrollment beginEnrollment();

    /**
     * Confirms a pending enrollment with a code from the authenticator app, activating enforcement and
     * returning a freshly-generated set of one-time recovery codes.
     *
     * @param code the current code from the authenticator app
     * @return the recovery codes, shown only this once
     */
    TotpRecoveryCodes confirmEnrollment(String code);

    /**
     * Disables and clears TOTP enrollment for the calling user's own account.
     */
    void disableTotp();

    /**
     * Returns whether the calling user currently has TOTP enrollment confirmed and active.
     *
     * @return true if enrolled
     */
    boolean isEnrolled();

}
