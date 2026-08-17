package dev.getelements.elements.service.auth.oidc;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.exception.auth.AuthValidationException;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import dev.getelements.elements.sdk.service.auth.OidcLinkService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static dev.getelements.elements.sdk.model.user.UserUid.USER_UID_CREATED_EVENT;

public class UserOidcAuthService implements OidcAuthService, OidcLinkService {

    private static final Logger logger = LoggerFactory.getLogger(UserOidcAuthService.class);

    private User user;

    private UserDao userDao;

    private UserUidDao userUidDao;

    private OidcAuthServiceOperations oidcAuthServiceOperations;

    private ElementRegistry elementRegistry;

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
        userUid.setVerificationStatus(VerificationStatus.VERIFIED);

        final var created = userUidDao.createUserUidStrict(userUid);
        getElementRegistry().publish(Event.builder()
                .argument(created)
                .named(USER_UID_CREATED_EVENT)
                .build());
    }

    private User apply(final DecodedJWT jwt, final OidcAuthScheme scheme) {
        return apply(user, jwt, scheme);
    }

    /**
     * Links the given already-authenticated user to the external OIDC identity carried by the token, rather than
     * the injected {@link #getUser() current user} — used by the browser-redirect login-attempt flow, where the
     * user to link was resolved at {@code begin()} time (when the caller's own session was known) and the
     * callback itself (hit by an unauthenticated provider redirect) has no session of its own.
     *
     * @param targetUser the already-authenticated user to link the external identity to
     * @param jwt the validated id_token
     * @param scheme the scheme the token was validated against
     * @return {@code targetUser}
     */
    public User apply(final User targetUser, final DecodedJWT jwt, final OidcAuthScheme scheme) {

        final var uid = OidcAuthServiceOperations.claimAsString(jwt, OidcAuthServiceOperations.Claim.USER_ID.value);
        final var email = OidcAuthServiceOperations.claimAsString(jwt, OidcAuthServiceOperations.Claim.EMAIL.value);

        // Check if this OIDC sub is already mapped to any user
        final var existingOidcUid = userUidDao.findUserUid(uid, scheme.getName());

        if (existingOidcUid.isPresent()) {
            final var linkedUserId = existingOidcUid.get().getUserId();
            if (linkedUserId != null && !linkedUserId.equals(targetUser.getId())) {
                throw new AuthValidationException("External OIDC identity is already linked to a different user.");
            }
            // Already linked to current user — idempotent, fall through
        } else {
            createNewUserUid(uid, scheme.getName(), targetUser.getId());
        }

        // Trust any email claim returned by a configured provider as verified — see AnonOidcAuthService for
        // rationale.
        if (email != null && !email.isEmpty()) {
            final var existingEmailUid = userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL);

            if (existingEmailUid.isEmpty()) {
                createNewUserUid(email, UserUidDao.SCHEME_EMAIL, targetUser.getId());
            } else {
                final var linkedUserId = existingEmailUid.get().getUserId();
                if (linkedUserId != null && !linkedUserId.equals(targetUser.getId())) {
                    // Stale or foreign mapping — skip rather than block the link operation
                    logger.warn("Email UID {} is already linked to a different user; skipping email UID creation.", email);
                }
                // else: already linked to current user — idempotent, do nothing
            }
        }

        // Tracking/audit snapshot of this scheme's reported profile claims — unlike email above, this isn't
        // identity-sensitive (no account-takeover concern), so it's safe to capture here too, unlike the flat
        // convenience fields on User which stay scoped to the anonymous login path (see AnonOidcAuthService).
        final var profileClaims = OidcAuthServiceOperations.extractProfileClaims(jwt);

        if (!profileClaims.isEmpty()) {
            var profiles = targetUser.getLinkedAccountProfiles();
            if (profiles == null) {
                profiles = new HashMap<String, Map<String, String>>();
                targetUser.setLinkedAccountProfiles(profiles);
            }
            profiles.put(scheme.getName(), profileClaims);

            try {
                getUserDao().updateUserStrict(targetUser);
            } catch (final Exception ex) {
                // Best-effort: this is an opportunistic profile-data capture, not a critical part of linking
                // the account. A pre-existing data issue on this user record must never block the link operation.
                logger.warn("Failed to persist linked account profile for user {}; continuing without it.",
                        targetUser.getId(), ex);
            }
        }

        return targetUser;

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

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserUidDao getUserUidDao() {
        return userUidDao;
    }

    @Inject
    public void setUserUidDao(UserUidDao userUidDao) {
        this.userUidDao = userUidDao;
    }

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
    }

}
