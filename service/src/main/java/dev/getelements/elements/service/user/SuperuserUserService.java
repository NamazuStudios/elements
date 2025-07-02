package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.*;
import dev.getelements.elements.rt.exception.BadRequestException;
import dev.getelements.elements.sdk.model.security.PasswordGenerator;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.sdk.service.user.UserService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;
import static dev.getelements.elements.util.PhoneNormalizer.normalizePhoneNb;
import static java.util.Collections.emptyList;

/**
 *
 *
 * Created by patricktwohig on 3/26/15.
 */
public class SuperuserUserService extends AbstractUserService implements UserService {

    private MapperRegistry mapperRegistry;

    private UserDao userDao;

    private SessionDao sessionDao;

    private NameService nameService;

    private PasswordGenerator passwordGenerator;

    private long sessionTimeoutSeconds;

    @Override
    public User getUser(String userId) {
        return getUserDao().getUser(userId);
    }

    @Override
    public Pagination<User> getUsers(int offset, int count) {
        return getUserDao().getUsers(offset, count);
    }

    @Override
    public Pagination<User> getUsers(int offset, int count, String search) {
        return getUserDao().getUsers(offset, count, search);
    }

    @Override
    public UserCreateResponse createUser(final UserCreateRequest userCreateRequest) {

        final var user = new User();

        //TODO: check is mapper or dozzer mapper would solve setting values issue
        user.setEmail(userCreateRequest.getEmail());
        user.setName(userCreateRequest.getName());
        user.setLevel(userCreateRequest.getLevel());
        user.setPrimaryPhoneNb(normalizePhoneNb(userCreateRequest.getPrimaryPhoneNb()).orElse(null));
        user.setFirstName(userCreateRequest.getFirstName());
        user.setLastName(userCreateRequest.getLastName());

        getNameService().assignNameAndEmailIfNecessary(user);

        // reuse existing DAO method
        final var password = isNullOrEmpty(userCreateRequest.getPassword())
            ? getPasswordGenerator().generate()
            : userCreateRequest.getPassword();

        final var created = getUserDao().createUserWithPasswordStrict(user, password);

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
        user.setEmail(userUpdateRequest.getEmail());
        user.setName(userUpdateRequest.getName());
        user.setLevel(userUpdateRequest.getLevel());
        user.setPrimaryPhoneNb(normalizePhoneNb(userUpdateRequest.getPrimaryPhoneNb()).orElse(null));
        user.setFirstName(userUpdateRequest.getFirstName());
        user.setLastName(userUpdateRequest.getLastName());

        final String password = nullToEmpty(userUpdateRequest.getPassword()).trim();

        return isNullOrEmpty(password) ?
            getUserDao().updateUser(user) :
            getUserDao().updateUser(user, password);

    }

    @Override
    public SessionCreation updateUserPassword(
            final String userId,
            final UserUpdatePasswordRequest userUpdatePasswordRequest) {
        throw new BadRequestException("Not implemented.");
    }

    @Override
    public void deleteUser(String userId) {
        getUserDao().softDeleteUser(userId);
    }

    public MapperRegistry getMapper() {
        return mapperRegistry;
    }

    @Inject
    public void setMapper(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public NameService getNameService() {
        return nameService;
    }

    @Inject
    public void setNameService(@Named(UNSCOPED) NameService nameService) {
        this.nameService = nameService;
    }

    public PasswordGenerator getPasswordGenerator() {
        return passwordGenerator;
    }

    @Inject
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Inject
    public void setSessionTimeoutSeconds(@Named(SESSION_TIMEOUT_SECONDS) long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

}
