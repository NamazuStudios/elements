package com.namazustudios.promotion.service.user;

import com.google.common.collect.Lists;
import com.namazustudios.promotion.dao.UserDao;
import com.namazustudios.promotion.exception.ForbiddenException;
import com.namazustudios.promotion.model.Pagination;
import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.service.UserService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 3/26/15.
 */
public class UserUserService extends AbstractUserService implements UserService {

    @Inject
    private UserDao userDao;

    @Override
    public User getUser(String userId) {
        checkForCurrentUser(userId);
        return getCurrentUser();
    }

    @Override
    public Pagination<User> getUsers(int offset, int count) {
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        } else if (offset == 0) {

            // The only user you are allowed to see is yourself.

            final Pagination<User> entry = new Pagination<>();
            entry.setOffset(0);
            entry.setTotal(1);
            entry.setObjects(Lists.newArrayList(getCurrentUser()));
            return entry;

        } else {
            return new Pagination<>();
        }
    }

    @Override
    public User createUser(User user) {
        throw new ForbiddenException();
    }

    @Override
    public User updateUser(User user) {

        checkForCurrentUser(user.getName());

        // Regular users cannot change their own level or change their name.  The underlying DAO
        // may support name changes, but this cannot be done here.

        user.setLevel(User.Level.USER);
        user.setName(getCurrentUser().getName());

        return userDao.updateUser(user);

    }

    @Override
    public void deleteUser(String userId) {
        // The user can only delete his or her own account.
        checkForCurrentUser(userId);
        userDao.deleteUser(userId);
    }

    @Override
    public User updateUserPassword(String userId, String password) {
        checkForCurrentUser(userId);
        return userDao.updateUserPassword(userId, password);
    }

}
