package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.UserUid;

/**
 * Anonymous implementation of {@link dev.getelements.elements.sdk.service.user.EmailVerificationService}.
 *
 * <p>Allows {@link #completeVerification} so that the public {@code GET /verify} endpoint works
 * without authentication.  {@link #requestVerification} always throws {@link ForbiddenException}.
 */
public class AnonEmailVerificationService extends AbstractEmailVerificationService {

    @Override
    public UserUid requestVerification(final String email, final String verificationBaseUrl) {
        throw new ForbiddenException("Anonymous users may not request email verification.");
    }

    @Override
    public UserUid completeVerification(final String token) {
        return doCompleteVerification(token);
    }

}
