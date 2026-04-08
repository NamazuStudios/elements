package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import jakarta.inject.Inject;

import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;

public class UserEmailVerificationService extends AbstractEmailVerificationService {

    private User currentUser;

    @Override
    public UserUid requestVerification(final String email, final String verificationBaseUrl) {
        final var uid = getUserUidDao().getUserUid(email, SCHEME_EMAIL);

        if (!getCurrentUser().getId().equals(uid.getUserId())) {
            throw new ForbiddenException("The specified email UID does not belong to the current user.");
        }

        return doRequestVerification(getCurrentUser(), email, verificationBaseUrl);
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
