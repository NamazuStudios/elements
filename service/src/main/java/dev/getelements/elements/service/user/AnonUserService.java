package dev.getelements.elements.service.user;

import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.model.user.*;
import dev.getelements.elements.security.PasswordGenerator;
import dev.getelements.elements.service.UserService;
import org.dozer.Mapper;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;

public class AnonUserService extends AbstractUserService implements UserService {

    private Mapper mapper;

    private UserDao userDao;

    private PasswordGenerator passwordGenerator;

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
    public UserCreateResponse createUser(final UserCreateRequest userCreateRequest)  {

        final var user = new User();
        user.setActive(true);
        user.setLevel(User.Level.USER);
        user.setEmail(userCreateRequest.getEmail());
        user.setName(userCreateRequest.getName());

        getNameService().assignNameAndEmailIfNecessary(user);

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

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public User updateUser(String userId, UserUpdateRequest userUpdateRequest) {
        throw new ForbiddenException();
    }

    @Override
    public SessionCreation updateUserPassword(String userId, UserUpdatePasswordRequest userUpdatePasswordRequest) {
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

    public PasswordGenerator getPasswordGenerator() {
        return passwordGenerator;
    }

    @Inject
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

}
