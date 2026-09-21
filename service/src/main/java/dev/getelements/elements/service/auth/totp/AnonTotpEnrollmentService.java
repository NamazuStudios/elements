package dev.getelements.elements.service.auth.totp;

import dev.getelements.elements.sdk.model.auth.TotpEnrollment;
import dev.getelements.elements.sdk.model.auth.TotpRecoveryCodes;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.service.auth.TotpEnrollmentService;

/**
 * Stub for unauthenticated callers. Never throws in the injected {@code get()} path (see
 * {@code TotpEnrollmentServiceProvider}) -- throws lazily here, at actual method-call time, instead.
 */
public class AnonTotpEnrollmentService implements TotpEnrollmentService {

    @Override
    public TotpEnrollment beginEnrollment() {
        throw new ForbiddenException("Must be signed in to enroll in TOTP two-factor authentication.");
    }

    @Override
    public TotpRecoveryCodes confirmEnrollment(final String code) {
        throw new ForbiddenException("Must be signed in to enroll in TOTP two-factor authentication.");
    }

    @Override
    public void disableTotp() {
        throw new ForbiddenException("Must be signed in to manage TOTP two-factor authentication.");
    }

    @Override
    public boolean isEnrolled() {
        throw new ForbiddenException("Must be signed in to manage TOTP two-factor authentication.");
    }

}
