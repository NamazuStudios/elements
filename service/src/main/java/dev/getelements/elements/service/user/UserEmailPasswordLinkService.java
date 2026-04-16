package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import dev.getelements.elements.sdk.service.user.EmailPasswordLinkService;
import jakarta.inject.Inject;

import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;

public class UserEmailPasswordLinkService implements EmailPasswordLinkService {

    private User currentUser;

    private UserDao userDao;

    private UserUidDao userUidDao;

    @Override
    public User linkEmailPassword(final String email, final String password) {
        final var normalizedEmail = email.trim().toLowerCase();
        final var uid = getUserUidDao().getUserUid(normalizedEmail, SCHEME_EMAIL);

        if (!getCurrentUser().getId().equals(uid.getUserId())) {
            throw new ForbiddenException("Email address belongs to a different account.");
        }

        if (!VerificationStatus.VERIFIED.equals(uid.getVerificationStatus())) {
            throw new ForbiddenException(
                "Email address must be verified before linking password credentials. " +
                "Call POST /user/me/email/verify first.");
        }

        return getUserDao().setPassword(getCurrentUser().getId(), password);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    @Inject
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserUidDao getUserUidDao() {
        return userUidDao;
    }

    @Inject
    public void setUserUidDao(UserUidDao userUidDao) {
        this.userUidDao = userUidDao;
    }

}
