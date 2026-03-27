package dev.getelements.elements.service.auth.oidc;

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

import java.util.Optional;

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

        userUidDao.createUserUid(userUid);
    }

    private Optional<User> tryGetUserFromUid(final Optional<UserUid> uid) {

        if (uid.isPresent()) {
            final var userId = uid.get().getUserId();

            if (userId != null) {
                final var user = userDao.getUser(userId);
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    private User apply(final DecodedJWT jwt, final OidcAuthScheme scheme) {

        final var uid = jwt.getClaim(OidcAuthServiceOperations.Claim.USER_ID.value).asString();
        final var email = jwt.getClaim(OidcAuthServiceOperations.Claim.EMAIL.value).asString();
        final var emailVerified = Boolean.TRUE.equals(jwt.getClaim("email_verified").asBoolean());

        // Search the existing UIDs to see if the user already exists
        final var oidcUid = userUidDao.findUserUid(uid, scheme.getName());

        // Only use email UID for lookup when email is verified to prevent account takeover
        final var emailUid = emailVerified && email != null && !email.isEmpty()
                ? userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL)
                : Optional.<UserUid>empty();

        var userOptional = tryGetUserFromUid(oidcUid);

        if (userOptional.isEmpty()) {
            userOptional = tryGetUserFromUid(emailUid);
        }

        // If the user already exists, associate any new UIDs from the JWT claims
        if (userOptional.isPresent()) {
            final var user = userOptional.get();

            if (oidcUid.isEmpty()) {
                createNewUserUid(uid, scheme.getName(), user.getId());
            }

            if (emailVerified && email != null && !email.isEmpty() && emailUid.isEmpty()) {
                createNewUserUid(email, UserUidDao.SCHEME_EMAIL, user.getId());
            }

            return user;
        }

        // No existing user — insert a fresh document via createUserStrict to avoid collision
        // when name/email are absent (createUser uses an upsert that would merge blank users).
        var user = new User();
        user.setLevel(USER);

        if (emailVerified && email != null && !email.isEmpty()) {
            user.setEmail(email);
        }

        user = getUserDao().createUserStrict(user);

        createNewUserUid(uid, scheme.getName(), user.getId());

        if (emailVerified && email != null && !email.isEmpty()) {
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
