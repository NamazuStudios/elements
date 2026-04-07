package dev.getelements.elements.service.auth.oidc;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.exception.auth.AuthValidationException;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserOidcAuthService implements OidcAuthService {

    private static final Logger logger = LoggerFactory.getLogger(UserOidcAuthService.class);

    private User user;

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

    private User apply(final DecodedJWT jwt, final OidcAuthScheme scheme) {

        final var uid = jwt.getClaim(OidcAuthServiceOperations.Claim.USER_ID.value).asString();
        final var email = jwt.getClaim(OidcAuthServiceOperations.Claim.EMAIL.value).asString();
        final var emailVerified = Boolean.TRUE.equals(jwt.getClaim("email_verified").asBoolean());

        // Check if this OIDC sub is already mapped to any user
        final var existingOidcUid = userUidDao.findUserUid(uid, scheme.getName());

        if (existingOidcUid.isPresent()) {
            final var linkedUserId = existingOidcUid.get().getUserId();
            if (linkedUserId != null && !linkedUserId.equals(user.getId())) {
                throw new AuthValidationException("External OIDC identity is already linked to a different user.");
            }
            // Already linked to current user — idempotent, fall through
        } else {
            createNewUserUid(uid, scheme.getName(), user.getId());
        }

        // Handle verified email UID
        if (emailVerified && email != null && !email.isEmpty()) {
            final var existingEmailUid = userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL);

            if (existingEmailUid.isEmpty()) {
                createNewUserUid(email, UserUidDao.SCHEME_EMAIL, user.getId());
            } else {
                final var linkedUserId = existingEmailUid.get().getUserId();
                if (linkedUserId != null && !linkedUserId.equals(user.getId())) {
                    // Stale or foreign mapping — skip rather than block the link operation
                    logger.warn("Email UID {} is already linked to a different user; skipping email UID creation.", email);
                }
                // else: already linked to current user — idempotent, do nothing
            }
        }

        return user;

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
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
