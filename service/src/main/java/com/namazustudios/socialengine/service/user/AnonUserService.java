package com.namazustudios.socialengine.service.user;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.UserCreateRequest;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;

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
    public User createUser(User user)  {
        throw new ForbiddenException();
    }

    @Override
    public User createUser(User user, String password)  {
        throw new ForbiddenException();
    }

    @Override
    public User createUser(final UserCreateRequest userCreateRequest)  {

        final User user = new User();

        user.setEmail(userCreateRequest.getEmail());
        user.setName(userCreateRequest.getName());

        user.setLevel(User.Level.USER);
        user.setActive(true);

        // reuse existing DAO method
        return getUserDao().createOrRectivateUserWithPassword(user, userCreateRequest.getPassword());

    }

    @Override
    public User updateUser(User user)  {
        throw new ForbiddenException();
    }

    @Override
    public User updateUser(User user, String password)  {
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
