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
        return userDao.getUser(userId);
    }

    @Override
    public Pagination<User> getUsers(int offset, int count) {
        return userDao.getUsers(offset, count);
    }

    @Override
    public User createUser(User user) {
        return userDao.createUser(user);
    }

    @Override
    public User updateUser(User user) {
        return userDao.updateActiveUser(user);
    }

    @Override
    public void deleteUser(String userId) {
        userDao.softDeleteUser(userId);
    }

    @Override
    public User updateUserPassword(String userId, String password) {
        return userDao.updateUserPassword(userId, password);
    }

}
