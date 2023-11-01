package dev.getelements.elements.service.user;

import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.SessionDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.model.user.*;
import dev.getelements.elements.security.PasswordGenerator;
import dev.getelements.elements.service.NameService;
import dev.getelements.elements.service.Unscoped;
import dev.getelements.elements.service.UserService;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Named;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 *
 * Created by patricktwohig on 3/26/15.
 */
public class SuperuserUserService extends AbstractUserService implements UserService {

    private Mapper mapper;

    private UserDao userDao;

    private SessionDao sessionDao;

    private NameService nameService;

    private PasswordGenerator passwordGenerator;

    private long sessionTimeoutSeconds;

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

        //TODO: check is mapper or dozzer mapper would solve setting values issue
        user.setEmail(userCreateRequest.getEmail());
        user.setName(userCreateRequest.getName());
        user.setLevel(userCreateRequest.getLevel());
        user.setPrimaryPhoneNb(userCreateRequest.getPrimaryPhoneNb());
        user.setFirstName(userCreateRequest.getFirstName());
        user.setLastName(userCreateRequest.getLastName());

        getNameService().assignNameAndEmailIfNecessary(user);

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
        user.setPrimaryPhoneNb(userUpdateRequest.getPrimaryPhoneNb());
        user.setFirstName(userUpdateRequest.getFirstName());
        user.setLastName(userUpdateRequest.getLastName());

        final String password = nullToEmpty(userUpdateRequest.getPassword()).trim();

        return isNullOrEmpty(password) ?
            getUserDao().updateActiveUser(user) :
            getUserDao().updateActiveUser(user, password);

    }

    @Override
    public SessionCreation updateUserPassword(
            final String userId,
            final UserUpdatePasswordRequest userUpdatePasswordRequest) {

        final var user = getUserDao().getActiveUser(userId);
        final var oldPassword = nullToEmpty(userUpdatePasswordRequest.getOldPassword()).trim();
        final var newPassword = nullToEmpty(userUpdatePasswordRequest.getNewPassword()).trim();

        final var profile = getProfileDao().getActiveProfile(userUpdatePasswordRequest.getProfileId());
        final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();

        final var session = new Session();
        session.setExpiry(expiry);
        session.setUser(user);
        session.setProfile(profile);
        session.setApplication(profile.getApplication());

        getUserDao().updateActiveUser(user, newPassword, oldPassword);
        return getSessionDao().create(session);

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
    public void setNameService(@Unscoped NameService nameService) {
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
