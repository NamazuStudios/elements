package com.namazustudios.socialengine.service.user;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.user.UserCreateRequest;
import com.namazustudios.socialengine.model.user.UserUpdateRequest;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

public class AnonUserService extends AbstractUserService implements UserService {

    @Inject
    private UserDao userDao;

    @Override
    public User getUser(String userId)  {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<User> getUsers(int offset, int count)  {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<User> getUsers(int offset, int count, String search)  {
        throw new ForbiddenException();
    }

    @Override
    public User createUser(final UserCreateRequest userCreateRequest)  {

        final User user = new User();

        user.setEmail(userCreateRequest.getEmail());
        user.setName(userCreateRequest.getName());

        user.setLevel(User.Level.USER);
        user.setActive(true);

        final String password = nullToEmpty(userCreateRequest.getPassword()).trim();

        return isNullOrEmpty(password) ?
                getUserDao().createOrReactivateUser(user) :
                getUserDao().createOrReactivateUserWithPassword(user, password);

    }

    @Override
    public User updateUser(String userId, UserUpdateRequest userUpdateRequest) {
        throw new ForbiddenException();
    }

    @Override
    public void deleteUser(String userId)  {
        throw new ForbiddenException();
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

}
