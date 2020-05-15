package com.namazustudios.socialengine.service.user;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.user.UserCreateRequest;
import com.namazustudios.socialengine.model.user.UserUpdateRequest;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * Created by patricktwohig on 3/26/15.
 */
public class SuperuserUserService extends AbstractUserService implements UserService {

    private UserDao userDao;

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
    public User createUser(final UserCreateRequest userCreateRequest) {

        final User user = new User();
        user.setEmail(userCreateRequest.getEmail());
        user.setName(userCreateRequest.getName());
        user.setLevel(userCreateRequest.getLevel());
        user.setActive(true);

        // reuse existing DAO method
        return getUserDao().createOrReactivateUserWithPassword(user, userCreateRequest.getPassword());

    }

    @Override
    public User updateUser(final String userId, UserUpdateRequest userUpdateRequest) {

        final User user = new User();

        user.setId(userId);
        user.setActive(true);
        user.setName(userUpdateRequest.getName());
        user.setLevel(userUpdateRequest.getLevel());

        final String password = nullToEmpty(userUpdateRequest.getPassword()).trim();

        return isNullOrEmpty(password) ?
                getUserDao().updateActiveUser(user) :
                getUserDao().updateActiveUser(user, password);

    }

    @Override
    public void deleteUser(String userId) {
        getUserDao().softDeleteUser(userId);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

}
