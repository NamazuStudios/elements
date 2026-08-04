package dev.getelements.elements.service.auth.oidc;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static dev.getelements.elements.sdk.model.user.UserUid.USER_UID_CREATED_EVENT;

public class AnonOidcAuthService implements OidcAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AnonOidcAuthService.class);

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

    public User apply(final DecodedJWT jwt, final OidcAuthScheme scheme) {

        final var uid = OidcAuthServiceOperations.claimAsString(jwt, OidcAuthServiceOperations.Claim.USER_ID.value);
        final var email = OidcAuthServiceOperations.claimAsString(jwt, OidcAuthServiceOperations.Claim.EMAIL.value);
        final var preferredUsername = OidcAuthServiceOperations.claimAsString(jwt, OidcAuthServiceOperations.Claim.PREFERRED_USERNAME.value);
        final var givenName = OidcAuthServiceOperations.claimAsString(jwt, OidcAuthServiceOperations.Claim.GIVEN_NAME.value);
        final var familyName = OidcAuthServiceOperations.claimAsString(jwt, OidcAuthServiceOperations.Claim.FAMILY_NAME.value);
        final var hasEmail = isPresent(email);
        final var profileClaims = OidcAuthServiceOperations.extractProfileClaims(jwt);

        // Search the existing UIDs to see if the user already exists
        final var oidcUid = userUidDao.findUserUid(uid, scheme.getName());

        // Trust any email claim returned by a configured provider as verified — the provider is trusted
        // infrastructure the admin explicitly configured, and Elements has no independent way to check a
        // provider's own email_verified claim (some providers omit it, or encode it as a non-boolean type).
        final var emailUid = hasEmail
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

            if (hasEmail && emailUid.isEmpty()) {
                createNewUserUid(email, UserUidDao.SCHEME_EMAIL, user.getId());
            }

            var changed = false;

            // Fill-only-if-blank: a returning user's existing value (set by an admin, the user, or an earlier
            // login) always wins over whatever a linked provider reports — this never overwrites, it only
            // fills in what a user created before the provider supplied these claims never got.
            if (hasEmail && isBlank(user.getEmail())) {
                user.setEmail(email);
                changed = true;
            }

            if (isPresent(preferredUsername) && isBlank(user.getPreferredUsername())) {
                user.setPreferredUsername(preferredUsername);
                changed = true;
            }

            if (isPresent(givenName) && isBlank(user.getFirstName())) {
                user.setFirstName(givenName);
                changed = true;
            }

            if (isPresent(familyName) && isBlank(user.getLastName())) {
                user.setLastName(familyName);
                changed = true;
            }

            // Unlike the fields above, this is a tracking/audit snapshot, not a "don't overwrite" convenience
            // field — always replaced wholesale with this scheme's latest reported profile claims.
            if (!profileClaims.isEmpty()) {
                putLinkedAccountProfile(user, scheme.getName(), profileClaims);
                changed = true;
            }

            if (changed) {
                try {
                    getUserDao().updateUserStrict(user);
                } catch (final Exception ex) {
                    // Best-effort: this is an opportunistic profile-data capture, not a critical part of
                    // authentication. A pre-existing data issue on this user record (e.g. a field that predates
                    // a validation rule tightened since the account was created) must never block login.
                    logger.warn("Failed to persist backfilled profile claims for user {}; continuing without them.",
                            user.getId(), ex);
                }
            }

            return user;
        }

        // No existing user — insert a fresh document via createUserStrict to avoid collision
        // when name/email are absent (createUser uses an upsert that would merge blank users).
        var user = new User();
        user.setLevel(USER);

        if (hasEmail) {
            user.setEmail(email);
        }

        if (isPresent(preferredUsername)) {
            user.setPreferredUsername(preferredUsername);
        }

        if (isPresent(givenName)) {
            user.setFirstName(givenName);
        }

        if (isPresent(familyName)) {
            user.setLastName(familyName);
        }

        if (!profileClaims.isEmpty()) {
            putLinkedAccountProfile(user, scheme.getName(), profileClaims);
        }

        user = getUserDao().createUserStrict(user);

        // If a stale OIDC UID exists (user was deleted), delete it before relinking
        if (oidcUid.isPresent()) {
            userUidDao.tryDeleteUserUid(oidcUid.get());
        }

        createNewUserUid(uid, scheme.getName(), user.getId());

        if (hasEmail) {
            // If a stale email UID exists (user was deleted), delete it before relinking
            if (emailUid.isPresent()) {
                userUidDao.tryDeleteUserUid(emailUid.get());
            }
            createNewUserUid(email, UserUidDao.SCHEME_EMAIL, user.getId());
        }

        return user;

    }

    private static boolean isPresent(final String value) {
        return value != null && !value.isEmpty();
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isEmpty();
    }

    private static void putLinkedAccountProfile(final User user, final String schemeName, final Map<String, String> claims) {
        var profiles = user.getLinkedAccountProfiles();
        if (profiles == null) {
            profiles = new HashMap<>();
            user.setLinkedAccountProfiles(profiles);
        }
        profiles.put(schemeName, claims);
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

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
    }

}
