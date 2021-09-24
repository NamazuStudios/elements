package com.namazustudios.socialengine.service.user;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.user.UserCreateRequest;
import com.namazustudios.socialengine.model.user.UserUpdateRequest;
import com.namazustudios.socialengine.service.ProfileService;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.service.UserService.formatAnonymousEmail;

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

        final var user = new User();
        user.setActive(true);
        user.setLevel(User.Level.USER);
        user.setEmail(userCreateRequest.getEmail());
        user.setName(userCreateRequest.getName());

        if (user.getName() == null || user.getEmail() == null) {
            final var name = getNameService().generateQualifiedName();
            if (user.getName() == null) user.setName(name);
            if (user.getEmail() == null) user.setEmail(formatAnonymousEmail(name));
        }

        // reuse existing DAO method
        final var created = isNullOrEmpty(userCreateRequest.getPassword()) ?
            getUserDao().createOrReactivateUser(user) :
            getUserDao().createOrReactivateUserWithPassword(user, userCreateRequest.getPassword());

        final var profiles = userCreateRequest.getProfiles();
        if (profiles != null) createProfiles(user.getId(), userCreateRequest.getProfiles());

        return created;

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
