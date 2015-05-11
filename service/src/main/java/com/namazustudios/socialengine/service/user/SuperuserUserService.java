package com.namazustudios.socialengine.service.user;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 3/26/15.
 */
public class SuperuserUserService extends AbstractUserService implements UserService {

    @Inject
    private UserDao userDao;

    @Override
    public User getUser(String userId) {
        return userDao.getActiveUser(userId);
    }

    @Override
    public Pagination<User> getUsers(int offset, int count) {
        return userDao.getActiveUsers(offset, count);
    }

    @Override
    public User createUser(User user) {
        return userDao.createOrActivateUser(user);
    }

    @Override
    public User createUser(User user, String password) {
        final User out = userDao.createOrActivateUser(user, password);
        userDao.validateActiveUserPassword(out.getName(), password);
        userDao.validateActiveUserPassword(out.getEmail(), password);
        return out;
    }

    @Override
    public User updateUser(User user) {
        return userDao.updateActiveUser(user);
    }

    @Override
    public User updateUser(User user, String password) {
        return userDao.updateActiveUser(user, password);
    }

    @Override
    public void deleteUser(String userId) {
        userDao.softDeleteUser(userId);
    }

    @Override
    public User updateUserPassword(String userId, String password) {
        return userDao.updateActiveUserPassword(userId, password);
    }

}
