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
    protected UserDao userDao;

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
    public User createUser(UserCreateRequest userCreateRequest)  {

        User user = new User();

        user.setEmail(userCreateRequest.getEmail());
        user.setName(userCreateRequest.getName());
        user.setFacebookId(userCreateRequest.getFacebookId());

        user.setLevel(User.Level.UNPRIVILEGED);
        user.setActive(true);

        // reuse existing DAO method
        return userDao.createOrRectivateUserWithPassword(user, userCreateRequest.getPassword());

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
}
