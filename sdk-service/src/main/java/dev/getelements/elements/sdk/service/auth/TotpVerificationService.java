package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.user.User;

/**
 * Standalone, identity-independent TOTP verification, used internally by the login flow before a caller has
 * an authenticated session. Behavior does not vary by caller identity, so (unlike most service interfaces)
 * this is bound identically for every access level rather than switched by a level-based
 * {@link jakarta.inject.Provider}. Deliberately does not extend any other bound service interface, to remain
 * bridgeable across the Guice-HK2 boundary for injection into JAX-RS resources.
 */
@ElementPublic
@ElementServiceExport
public interface TotpVerificationService {

    /**
     * Returns whether a login for the given user must be challenged for a TOTP code -- true only when TOTP is
     * enabled system-wide and the user has confirmed enrollment.
     *
     * @param user the user attempting to log in
     * @return true if a second-factor challenge is required
     */
    boolean isRequiredFor(User user);

    /**
     * Verifies a code for the given user, checking it first as a current TOTP code and, failing that, as a
     * one-time recovery code (consuming it if it matches).
     *
     * @param user the user completing the challenge
     * @param code the submitted code
     * @return true if the code was valid
     */
    boolean verify(User user, String code);

}
