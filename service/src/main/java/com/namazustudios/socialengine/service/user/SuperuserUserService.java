package com.namazustudios.socialengine.service.user;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.user.UserCreateRequest;
import com.namazustudios.socialengine.model.user.UserCreateResponse;
import com.namazustudios.socialengine.model.user.UserUpdateRequest;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.service.NameService;
import com.namazustudios.socialengine.service.UserService;
import org.dozer.Mapper;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.service.UserService.formatAnonymousEmail;
import static java.util.Collections.emptyList;

/**
 *
 *
 * Created by patricktwohig on 3/26/15.
 */
public class SuperuserUserService extends AbstractUserService implements UserService {

    private Mapper mapper;

    private UserDao userDao;

    private NameService nameService;

    private PasswordGenerator passwordGenerator;

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
    public UserCreateResponse createUser(final UserCreateRequest userCreateRequest) {

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
        final var password = isNullOrEmpty(userCreateRequest.getPassword())
            ? getPasswordGenerator().generate()
            : userCreateRequest.getPassword();

        final var created = getUserDao().createOrReactivateUserWithPassword(user, password);

        final var response = getMapper().map(created, UserCreateResponse.class);
        response.setPassword(password);

        final var profiles = userCreateRequest.getProfiles();

        if (profiles == null) {
            response.setProfiles(emptyList());
        } else {
            final var createdProfiles = createProfiles(created.getId(), userCreateRequest.getProfiles());
            response.setProfiles(createdProfiles);
        }

        return response;

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

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
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

    public PasswordGenerator getPasswordGenerator() {
        return passwordGenerator;
    }

    @Inject
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

}
