package com.namazustudios.socialengine.service.user;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.CreateProfileRequest;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.user.UserCreateRequest;
import com.namazustudios.socialengine.model.user.UserUpdateRequest;
import com.namazustudios.socialengine.service.NameService;
import com.namazustudios.socialengine.service.ProfileService;
import com.namazustudios.socialengine.service.UserService;
import com.namazustudios.socialengine.service.profile.SuperUserProfileService;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.service.UserService.formatAnonymousEmail;

/**
 *
 *
 * Created by patricktwohig on 3/26/15.
 */
public class SuperuserUserService extends AbstractUserService implements UserService {

    private UserDao userDao;

    private NameService nameService;

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

        final var user = new User();
        user.setActive(true);
        user.setEmail(userCreateRequest.getEmail());
        user.setName(userCreateRequest.getName());
        user.setLevel(userCreateRequest.getLevel());

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
        if (profiles != null) createProfiles(created.getId(), userCreateRequest.getProfiles());

        return created;

    }

    @Override
    public User updateUser(final String userId, final UserUpdateRequest userUpdateRequest) {

        final var user = new User();

        user.setId(userId);
        user.setActive(true);
        user.setEmail(userUpdateRequest.getEmail());
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

    public NameService getNameService() {
        return nameService;
    }

    @Inject
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }

}
