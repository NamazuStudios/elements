package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.user.UsernamePasswordLinkService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_NAME;

public class UserUsernamePasswordLinkService implements UsernamePasswordLinkService {

    private User currentUser;

    private UserDao userDao;

    private UserUidDao userUidDao;

    private Provider<Transaction> transactionProvider;

    @Override
    public User linkUsernamePassword(final String username, final String password) {
        final var normalizedUsername = username.trim();
        final var existingName = getCurrentUser().getName();

        if (existingName != null && !existingName.isBlank() && !existingName.equals(normalizedUsername)) {
            throw new ForbiddenException(
                "The supplied username does not match this account's existing name. " +
                "Name changes must be performed explicitly.");
        }

        if (existingName == null || existingName.isBlank()) {
            final var uid = new UserUid();
            uid.setId(normalizedUsername);
            uid.setScheme(SCHEME_NAME);
            uid.setUserId(getCurrentUser().getId());

            final var userUpdate = new User();
            userUpdate.setId(getCurrentUser().getId());
            userUpdate.setName(normalizedUsername);
            userUpdate.setEmail(getCurrentUser().getEmail());
            userUpdate.setLevel(getCurrentUser().getLevel());

            try {
                return getTransactionProvider().get().performAndClose(tx -> {
                    tx.getDao(UserUidDao.class).createUserUidStrict(uid);
                    return tx.getDao(UserDao.class).updateUser(userUpdate, password);
                });
            } catch (DuplicateException e) {
                throw new ForbiddenException("Username '" + normalizedUsername + "' is already taken.");
            }
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

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}
