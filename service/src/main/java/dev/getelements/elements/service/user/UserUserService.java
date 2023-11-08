package dev.getelements.elements.service.user;

import com.google.common.collect.Lists;
import dev.getelements.elements.dao.SessionDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.model.user.*;
import dev.getelements.elements.service.UserService;

import javax.inject.Inject;
import javax.inject.Named;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by patricktwohig on 3/26/15.
 */
public class UserUserService extends AnonUserService implements UserService {

    private SessionDao sessionDao;

    private long sessionTimeoutSeconds;

    @Override
    public User getUser(String userId) {
        checkForCurrentUser(userId);
        return getCurrentUser();
    }

    @Override
    public Pagination<User> getUsers(int offset, int count) {
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        } else if (offset == 0) {

            // The only user you are allowed to see is yourself.

            final Pagination<User> entry = new Pagination<>();
            entry.setOffset(0);
            entry.setTotal(1);
            entry.setObjects(Lists.newArrayList(getCurrentUser()));
            return entry;

        } else {
            return new Pagination<>();
        }
    }

    @Override
    public Pagination<User> getUsers(int offset, int count, String search) {
        return getUsers(offset, count);
    }

    @Override
    public UserCreateResponse createUser(UserCreateRequest user) {
        throw new ForbiddenException();
    }

    @Override
    public User updateUser(final String userId, final UserUpdateRequest userUpdateRequest) {

        checkForCurrentUser(userId);

        final User user = new User();

        //TODO: problem found here. Data were never update for user, changed phone, firstName and lastName to be updateable.
        // What about rest?
        user.setId(userId);
        user.setActive(true);
        user.setLevel(User.Level.USER);
        user.setName(getCurrentUser().getName());
        user.setEmail(getCurrentUser().getEmail());
        user.setPrimaryPhoneNb(userUpdateRequest.getPrimaryPhoneNb());
        user.setFirstName(userUpdateRequest.getFirstName());
        user.setLastName(userUpdateRequest.getLastName());

        return getUserDao().updateActiveUser(user);

    }

    @Override
    public SessionCreation updateUserPassword(final String userId,
                                              final UserUpdatePasswordRequest userUpdatePasswordRequest) {

        checkForCurrentUser(userId);

        final var user = getUserDao().getActiveUser(userId);
        final var profileId = userUpdatePasswordRequest.getProfileId();
        final var oldPassword = nullToEmpty(userUpdatePasswordRequest.getOldPassword()).trim();
        final var newPassword = nullToEmpty(userUpdatePasswordRequest.getNewPassword()).trim();
        final var profile = getProfileDao().findActiveProfileForUser(profileId, userId);

        final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();

        final var session = new Session();
        session.setExpiry(expiry);
        session.setUser(user);
        session.setProfile(profile.orElse(null));
        session.setApplication(profile.map(p -> p.getApplication()).orElse(null));

        getUserDao().updateActiveUser(user, newPassword, oldPassword);
        return getSessionDao().create(session);

    }

    @Override
    public void deleteUser(String userId) {
        // The user can only delete his or her own account.
        checkForCurrentUser(userId);
        getUserDao().softDeleteUser(userId);
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Inject
    public void setSessionTimeoutSeconds(@Named(SESSION_TIMEOUT_SECONDS) long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

}
