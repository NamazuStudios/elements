package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import jakarta.inject.Inject;

import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;
import static dev.getelements.elements.sdk.model.user.UserUid.USER_UID_CREATED_EVENT;

public class UserEmailVerificationService extends AbstractEmailVerificationService {

    private User currentUser;

    @Override
    public UserUid requestVerification(final String email, final String verificationBaseUrl) {
        final var normalizedEmail = email.trim().toLowerCase();
        final var existing = getUserUidDao().findUserUid(normalizedEmail, SCHEME_EMAIL);

        if (existing.isPresent()) {
            final var uid = existing.get();
            if (!getCurrentUser().getId().equals(uid.getUserId())) {
                throw new ForbiddenException("The specified email address is already linked to a different account.");
            }
            // No early-return for VERIFIED: if the caller explicitly requests verification (e.g. to
            // set up email+password auth after OIDC sign-up), they must go through our own email
            // flow even if the UID was already marked VERIFIED by an OIDC provider.
        } else {
            final var newUid = new UserUid();
            newUid.setId(normalizedEmail);
            newUid.setScheme(SCHEME_EMAIL);
            newUid.setUserId(getCurrentUser().getId());
            newUid.setVerificationStatus(VerificationStatus.UNVERIFIED);
            final var created = getUserUidDao().createUserUidStrict(newUid);
            getElementRegistry().publish(Event.builder()
                    .argument(created)
                    .named(USER_UID_CREATED_EVENT)
                    .build());
        }

        return doRequestVerification(getCurrentUser(), normalizedEmail, verificationBaseUrl);
    }

    @Override
    public UserUid completeVerification(final String token) {
        return doCompleteVerification(token);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    @Inject
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

}
