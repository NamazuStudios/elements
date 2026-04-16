package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.user.UsernamePasswordLinkService;
import jakarta.inject.Inject;

import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_NAME;

/**
 * Superuser (and UNSCOPED) implementation of {@link UsernamePasswordLinkService}.
 *
 * <p>Unlike the user-level implementation, this variant does not require a current-user context.
 * It resolves the target user by looking up the existing SCHEME_NAME UID for the given username,
 * then sets the password on that user. Suitable for Element code that provisions credentials
 * programmatically. If no UID exists for the username, a {@code NotFoundException} is thrown.
 */
public class SuperUserUsernamePasswordLinkService implements UsernamePasswordLinkService {

    private UserDao userDao;

    private UserUidDao userUidDao;

    @Override
    public User linkUsernamePassword(final String username, final String password) {
        final var normalizedUsername = username.trim();
        final var uid = getUserUidDao().getUserUid(normalizedUsername, SCHEME_NAME);
        return getUserDao().setPassword(uid.getUserId(), password);
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
