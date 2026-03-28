package dev.getelements.elements.service.auth.oauth2;

import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.auth.AuthValidationException;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.auth.OAuth2AuthService;
import jakarta.inject.Inject;

import java.util.Optional;

public class UserOAuth2AuthService implements OAuth2AuthService {

    private User user;

    private UserDao userDao;

    private UserUidDao userUidDao;

    private OAuth2AuthServiceOperations oAuth2AuthServiceOperations;

    @Override
    public SessionCreation createSession(OAuth2SessionRequest oAuth2SessionRequest) {
        return getOAuth2AuthServiceOperations().createOrUpdateUserWithToken(oAuth2SessionRequest, this::apply);
    }

    private User apply(String scheme, String uid) {

        // Check if this uid/scheme is already mapped to an existing user
        final var existingUid = userUidDao.findUserUid(uid, scheme);
        final var existingUser = tryGetUserFromUid(existingUid);

        if (existingUser.isPresent()) {
            final var found = existingUser.get();
            if (!found.getId().equals(user.getId())) {
                throw new AuthValidationException("External uid is already linked to a different user.");
            }
            return found;
        }

        // Not yet linked — associate the external uid with the currently authenticated user.
        // Delete any stale UID entry first if one exists (e.g. the previous user was soft-deleted).
        if (existingUid.isPresent()) {
            userUidDao.tryDeleteUserUid(existingUid.get());
        }

        createNewUserUid(uid, scheme, user.getId());

        return user;
    }

    private void createNewUserUid(String uid, String scheme, String userId) {
        final var userUid = new UserUid();
        userUid.setUserId(userId);
        userUid.setId(uid);
        userUid.setScheme(scheme);

        userUidDao.createUserUid(userUid);
    }

    private Optional<User> tryGetUserFromUid(final Optional<UserUid> uid) {

        if(uid.isPresent()) {
            final var userId = uid.get().getUserId();

            if(userId != null) {
                final var user = userDao.getUser(userId);
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public OAuth2AuthServiceOperations getOAuth2AuthServiceOperations() {
        return oAuth2AuthServiceOperations;
    }

    @Inject
    public void setOAuth2AuthServiceOperations(OAuth2AuthServiceOperations oAuth2AuthServiceOperations) {
        this.oAuth2AuthServiceOperations = oAuth2AuthServiceOperations;
    }

    public UserUidDao getUserUidDao() {
        return userUidDao;
    }

    @Inject
    public void setUserUidDao(UserUidDao userUidDao) {
        this.userUidDao = userUidDao;
    }

}
