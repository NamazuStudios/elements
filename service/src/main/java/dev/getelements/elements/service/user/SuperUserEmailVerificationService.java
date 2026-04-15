package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;

import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;

/**
 * Superuser (and UNSCOPED) implementation of {@link dev.getelements.elements.sdk.service.user.EmailVerificationService}.
 *
 * <p>Handles both {@link #requestVerification} (no ownership check — useful when acting on behalf of
 * any user, e.g. from Element code) and {@link #completeVerification} (token consumption).
 */
public class SuperUserEmailVerificationService extends AbstractEmailVerificationService {

    @Override
    public UserUid requestVerification(final String email, final String verificationBaseUrl) {
        final var uid = getUserUidDao().getUserUid(email, SCHEME_EMAIL);

        final var ownerUser = new User();
        ownerUser.setId(uid.getUserId());

        return doRequestVerification(ownerUser, email, verificationBaseUrl);
    }

    @Override
    public UserUid completeVerification(final String token) {
        return doCompleteVerification(token);
    }

}
