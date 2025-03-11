package dev.getelements.elements.service.user;

import com.google.common.collect.Lists;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.*;
import dev.getelements.elements.sdk.service.user.UserService;
import dev.getelements.elements.util.PhoneNormalizer;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
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

        user.setId(userId);
        user.setLevel(User.Level.USER);
        user.setName(userUpdateRequest.getName());
        user.setEmail(userUpdateRequest.getEmail());
        user.setPrimaryPhoneNb(PhoneNormalizer.normalizePhoneNb(userUpdateRequest.getPrimaryPhoneNb()).orElse(null));
        user.setFirstName(userUpdateRequest.getFirstName());
        user.setLastName(userUpdateRequest.getLastName());

        return getUserDao().updateUser(user);

    }

    @Override
    public SessionCreation updateUserPassword(final String userId,
                                              final UserUpdatePasswordRequest userUpdatePasswordRequest) {

        checkForCurrentUser(userId);

        final var user = getUserDao().getUser(userId);
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

        getUserDao().updateUser(user, newPassword, oldPassword);
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
