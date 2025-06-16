package dev.getelements.elements.service.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static dev.getelements.elements.sdk.model.user.User.Level.USER;

public class AnonOidcAuthService implements OidcAuthService {

    private UserDao userDao;

    private UserUidDao userUidDao;

    private OidcAuthServiceOperations oidcAuthServiceOperations;

    @Override
    public SessionCreation createSession(OidcSessionRequest oidcSessionRequest) {
        return getOidcAuthServiceOperations().createOrUpdateUserWithToken(
                oidcSessionRequest,
                this::apply
        );
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

    private User apply(final DecodedJWT jwt, final OidcAuthScheme scheme) {

        final var uid = jwt.getClaim(OidcAuthServiceOperations.Claim.USER_ID.value).asString();
        final var email = jwt.getClaim(OidcAuthServiceOperations.Claim.EMAIL.value).asString();

        //Search the existing UIds to see if the user already exists
        var oidcUid = userUidDao.findUserUid(uid, scheme.getName());
        var emailUid = userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL);

        var userOptional = tryGetUserFromUid(oidcUid);

        if (userOptional.isEmpty()) {
            userOptional = tryGetUserFromUid(emailUid);
        }

        //If the user already exists, check to see if we need to associate
        //any new UIds from the extracted JWT claims
        if (userOptional.isPresent()) {
            final var user = userOptional.get();

            if (oidcUid.isEmpty()) {
                createNewUserUid(uid, scheme.getName(), user.getId());
            }

            if (emailUid.isEmpty() && !email.isEmpty()) {
                createNewUserUid(email, UserUidDao.SCHEME_EMAIL, user.getId());
            }

            return user;
        }

        //No existing user was found, create a new one in the DB and assign the ref to
        //any UIds made from the JWT claims
        var user = new User();
        user.setName(email);
        user.setEmail(email);
        user.setLevel(USER);
        user = getUserDao().createUser(user);

        createNewUserUid(uid, scheme.getName(), user.getId());

        if (!email.isEmpty()) {
            createNewUserUid(email, UserUidDao.SCHEME_EMAIL, user.getId());
        }

        return user;

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public OidcAuthServiceOperations getOidcAuthServiceOperations() {
        return oidcAuthServiceOperations;
    }

    @Inject
    public void setOidcAuthServiceOperations(OidcAuthServiceOperations oidcAuthServiceOperations) {
        this.oidcAuthServiceOperations = oidcAuthServiceOperations;
    }

    public UserUidDao getUserUidDao() {
        return userUidDao;
    }

    @Inject
    public void setUserUidDao(UserUidDao userUidDao) {
        this.userUidDao = userUidDao;
    }

}
