package com.namazustudios.socialengine.service.user;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 3/26/15.
 */
public class SuperuserUserService extends UserUserService implements UserService {

    @Override
    public User getUser(String userId) {
        return getUserDao().getActiveUser(userId);
    }

    @Override
    public Pagination<User> getUsers(int offset, int count) {
        return getUserDao().getActiveUsers(offset, count);
    }

    @Override
    public Pagination<User> getUsers(int offset, int count, String search) {
        return getUserDao().getActiveUsers(offset, count, search);
    }

    @Override
    public User createUser(User user) {
        return getUserDao().createOrReactivateUser(user);
    }

    @Override
    public User createUser(User user, String password) {
        return getUserDao().createOrRectivateUserWithPassword(user, password);
    }

    @Override
    public User updateUser(User user) {
        return getUserDao().updateActiveUser(user);
    }

    @Override
    public User updateUser(User user, String password) {
        return getUserDao().updateActiveUser(user, password);
    }

    @Override
    public void deleteUser(String userId) {
        getUserDao().softDeleteUser(userId);
    }

}
