package dev.getelements.elements.service.auth.oauth2;

import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.auth.OAuth2AuthService;
import jakarta.inject.Inject;

import java.util.Optional;

import static dev.getelements.elements.sdk.model.user.User.Level.USER;

public class AnonOAuth2AuthService implements OAuth2AuthService {

    private UserDao userDao;

    private UserUidDao userUidDao;

    private OAuth2AuthServiceOperations oAuth2AuthServiceOperations;

    @Override
    public SessionCreation createSession(OAuth2SessionRequest oAuth2SessionRequest) {
        return getOAuth2AuthServiceOperations().createOrUpdateUserWithToken(oAuth2SessionRequest, this::apply);
    }

    private User apply(String scheme, String uid) {

        //Search the existing UIds to see if the user already exists
        final var oidcUid = userUidDao.findUserUid(uid, scheme);
        final var userOptional = tryGetUserFromUid(oidcUid);

        //If the user already exists, check to see if we need to associate any new UIds
        if (userOptional.isPresent()) {
            final var user = userOptional.get();

            if (oidcUid.isEmpty()) {
                createNewUserUid(uid, scheme, user.getId());
            }

            return user;
        }

        //No existing user was found, create a new one in the DB and assign the ref to any UIds made
        var user = new User();
        user.setLevel(USER);
        user = getUserDao().createUser(user);

        createNewUserUid(uid, scheme, user.getId());

        return user;
    }

    private void createNewUserUid(String uid, String scheme, String userId) {
        final var userUid = new UserUid();
        userUid.setUserId(userId);
        userUid.setId(uid);
        userUid.setScheme(scheme);

        userUidDao.createUserUidStrict(userUid);
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

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setOAuth2UserDao(UserDao userDao) {
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

