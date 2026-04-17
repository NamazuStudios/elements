package dev.getelements.elements.service.user;

/**
 * Anonymous implementation of {@link dev.getelements.elements.sdk.service.user.PasswordResetService}.
 *
 * <p>Both {@link #requestReset} and {@link #completeReset} are public operations that require no
 * authentication — the reset flow itself serves as proof of email ownership.
 */
public class AnonPasswordResetService extends AbstractPasswordResetService {

    @Override
    public void requestReset(final String email, final String resetBaseUrl) {
        doRequestReset(email, resetBaseUrl);
    }

    @Override
    public void completeReset(final String token, final String newPassword) {
        doCompleteReset(token, newPassword);
    }

}
