package dev.getelements.elements.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import dev.getelements.elements.sdk.service.auth.OidcAuthSchemeService;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.sdk.service.util.CryptoKeyPairUtility;
import dev.getelements.elements.service.auth.oidc.AnonOidcAuthService;
import dev.getelements.elements.service.util.ServicesMapperRegistryProvider;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static dev.getelements.elements.sdk.service.Constants.OIDC_JWKS_REFRESH_SECONDS;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static java.lang.System.currentTimeMillis;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class OidcAccountLinkingTest {

    private static final String SCHEME_NAME = "TestIssuer";
    private static final String ISSUER     = "https://test.issuer.com";
    private static final String KID        = "test-key-1";

    private static final String SCHEME_NAME_2 = "TestIssuer2";
    private static final String ISSUER_2      = "https://test2.issuer.com";
    private static final String KID_2         = "test-key-2";

    private RSAPublicKey publicKey;
    private Algorithm   algorithm;

    private RSAPublicKey publicKey2;
    private Algorithm   algorithm2;

    @Inject
    private Provider<AnonOidcAuthService> anonServiceProvider;

    @Inject
    private SessionDao sessionDao;

    @Inject
    private UserDao userDao;

    @Inject
    private UserUidDao userUidDao;

    @Inject
    private OidcAuthSchemeDao oidcAuthSchemeDao;

    private UserCreation userCreation;

    @BeforeClass
    public void setupKeys() throws Exception {
        final var kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);

        final var kp  = kpg.generateKeyPair();
        publicKey  = (RSAPublicKey) kp.getPublic();
        algorithm  = Algorithm.RSA256(publicKey, (RSAPrivateKey) kp.getPrivate());

        final var kp2 = kpg.generateKeyPair();
        publicKey2 = (RSAPublicKey) kp2.getPublic();
        algorithm2 = Algorithm.RSA256(publicKey2, (RSAPrivateKey) kp2.getPrivate());
    }

    @BeforeClass(dependsOnMethods = "setupKeys")
    public void setupInjector() {
        createInjector(new TestModule()).injectMembers(this);
        when(oidcAuthSchemeDao.findAuthScheme(ISSUER)).thenReturn(Optional.of(buildScheme()));
        when(oidcAuthSchemeDao.findAuthScheme(ISSUER_2)).thenReturn(Optional.of(buildScheme2()));
    }

    @BeforeMethod
    public void resetMocks() {
        reset(sessionDao, userDao, userUidDao);
        when(userUidDao.createUserUidStrict(any(UserUid.class))).then(i -> i.getArgument(0));
        when(sessionDao.create(any())).then(i -> {
            final var sc = new SessionCreation();
            sc.setSession(i.getArgument(0));
            sc.setSessionSecret("secret");
            return sc;
        });

        // newEmptyUser() returns a fluent builder; RETURNS_SELF makes every chained setter return the
        // same mock so calls can be verified individually without hand-rolling a fake implementation.
        userCreation = mock(UserCreation.class, RETURNS_SELF);
        when(userDao.newEmptyUser()).thenReturn(userCreation);
    }

    // ── scenario 1: brand-new user ────────────────────────────────────────────

    @Test
    public void testNewUserCreatesOidcAndEmailUid() {
        final var uid   = randomId();
        final var email = "new@example.com";
        final var newId = randomId();

        when(userUidDao.findUserUid(uid,   SCHEME_NAME))              .thenReturn(Optional.empty());
        when(userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL))  .thenReturn(Optional.empty());
        when(userCreation.create()).thenReturn(user(new User(), newId));

        assertNotNull(session(uid, email));

        verify(userDao, never()).createUserStrict(any());
        verify(userCreation).uid(SCHEME_NAME, uid, VerificationStatus.VERIFIED);
        verify(userCreation).uid(UserUidDao.SCHEME_EMAIL, email, VerificationStatus.VERIFIED);
    }

    // ── scenario 2: returning user with matching OIDC UID ─────────────────────

    @Test
    public void testReturningOidcUserNoNewUidsCreated() {
        final var uid    = randomId();
        final var email  = "returning@example.com";
        final var userId = randomId();

        when(userUidDao.findUserUid(uid,   SCHEME_NAME))             .thenReturn(Optional.of(uid(uid,   SCHEME_NAME,             userId)));
        when(userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL)) .thenReturn(Optional.of(uid(email, UserUidDao.SCHEME_EMAIL, userId)));
        when(userDao.getUser(userId)).thenReturn(existingUser(userId));

        final var result = session(uid, email);
        assertNotNull(result);
        assertEquals(result.getSession().getUser().getId(), userId);

        verify(userDao, never()).createUserStrict(any());
        verify(userUidDao, never()).createUserUidStrict(any());
    }

    // ── scenario 2b: returning user whose account predates the provider supplying an email ──────────
    //   the OIDC UID already resolves to an existing user with no email set (e.g. created before the
    //   provider config requested email claims) — a later login that now includes an email must backfill it
    //   onto the existing User record, not just leave it linked via the UserUid.

    @Test
    public void testReturningUserBackfillsEmailWhenNewlyProvided() {
        final var uid    = randomId();
        final var email  = "backfilled@example.com";
        final var userId = randomId();

        when(userUidDao.findUserUid(uid,   SCHEME_NAME))             .thenReturn(Optional.of(uid(uid,   SCHEME_NAME, userId)));
        when(userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL)) .thenReturn(Optional.empty());
        when(userDao.getUser(userId)).thenReturn(existingUser(userId));

        final var result = session(uid, email);
        assertNotNull(result);

        verify(userDao).updateUserStrict(argThat(u -> email.equals(u.getEmail())));
        verify(userUidDao).createUserUidStrict(argThat(u -> UserUidDao.SCHEME_EMAIL.equals(u.getScheme()) && email.equals(u.getId())));
    }

    // ── scenario 3: account linking via email ─────────────────────────────────
    //   user exists (has an email UID) but logs in via a new OIDC scheme for
    //   the first time → OIDC UID should be added to the existing user

    @Test
    public void testEmailLinkingAddsOidcUidToExistingUser() {
        final var uid    = randomId();
        final var email  = "linked@example.com";
        final var userId = randomId();

        when(userUidDao.findUserUid(uid,   SCHEME_NAME))             .thenReturn(Optional.empty());
        when(userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL)) .thenReturn(Optional.of(uid(email, UserUidDao.SCHEME_EMAIL, userId)));
        when(userDao.getUser(userId)).thenReturn(existingUser(userId));

        final var result = session(uid, email);
        assertNotNull(result);
        assertEquals(result.getSession().getUser().getId(), userId);

        verify(userDao, never()).createUserStrict(any());
        verify(userUidDao).createUserUidStrict(argThat(u -> SCHEME_NAME.equals(u.getScheme()) && uid.equals(u.getId())));
        verify(userUidDao, never()).createUserUidStrict(argThat(u -> UserUidDao.SCHEME_EMAIL.equals(u.getScheme())));
    }

    // Note: reclaiming a stale/orphaned email UID (present but unresolvable to any user, e.g. left behind
    // by a prior delete) is now handled entirely inside MongoUserDao's newEmptyUser() builder, not by
    // AnonOidcAuthService — it always just requests the link via UserCreation#uid(...) regardless of
    // whether a stale row exists, and the DAO decides whether to reclaim it or reject a genuine conflict.
    // See MongoUserCreationTest (mongo-test) for coverage of that resolution logic.

    // ── scenario 5: JWT has no email claim ────────────────────────────────────

    @Test
    public void testNullEmailSkipsEmailUidLookupAndCreation() {
        final var uid      = randomId();
        final var newUserId = randomId();

        when(userUidDao.findUserUid(uid, SCHEME_NAME)).thenReturn(Optional.empty());
        when(userCreation.create()).thenReturn(user(new User(), newUserId));

        assertNotNull(session(uid, null));

        verify(userDao, never()).createUserStrict(any());
        verify(userCreation).uid(SCHEME_NAME, uid, VerificationStatus.VERIFIED);
        verify(userUidDao, never()).findUserUid(anyString(), eq(UserUidDao.SCHEME_EMAIL));
        verify(userCreation, never()).uid(eq(UserUidDao.SCHEME_EMAIL), anyString(), any());
    }

    // ── scenario 6: email present but not marked verified ────────────────────
    //   the provider is trusted infrastructure the admin configured; any email claim it returns is linked and
    //   trusted regardless of email_verified (which some providers omit or encode as a non-boolean type)

    @Test
    public void testUnverifiedEmailIsStillLinkedAndTrusted() {
        final var uid   = randomId();
        final var email = "unverified@example.com";
        final var newId = randomId();

        when(userUidDao.findUserUid(uid,   SCHEME_NAME))              .thenReturn(Optional.empty());
        when(userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL))  .thenReturn(Optional.empty());
        when(userCreation.create()).thenReturn(user(new User(), newId));

        final var jwt = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(uid)
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000))
                .withClaim("email", email)
                .withClaim("email_verified", false)
                .sign(algorithm);

        final var request = new OidcSessionRequest();
        request.setJwt(jwt);

        assertNotNull(anonServiceProvider.get().createSession(request));

        verify(userCreation).email(email);
        verify(userCreation).uid(UserUidDao.SCHEME_EMAIL, email, VerificationStatus.VERIFIED);
    }

    // ── scenario 7: new user captures profile claims + per-scheme snapshot ────

    @Test
    public void testNewUserCapturesProfileClaimsAndLinkedAccountProfile() {
        final var uid = randomId();
        final var email = "profile@example.com";
        final var newId = randomId();

        when(userUidDao.findUserUid(uid,   SCHEME_NAME))             .thenReturn(Optional.empty());
        when(userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL)) .thenReturn(Optional.empty());
        when(userCreation.create()).thenReturn(user(new User(), newId));

        assertNotNull(sessionWithProfileClaims(uid, email, "Pat", "Doe", "patdoe"));

        verify(userCreation).displayName("patdoe");
        verify(userCreation).firstName("Pat");
        verify(userCreation).lastName("Doe");
        verify(userCreation).linkedAccountProfile(eq(SCHEME_NAME), argThat(claims ->
                "patdoe".equals(claims.get("preferred_username"))
                        && "Pat".equals(claims.get("given_name"))
                        && "Doe".equals(claims.get("family_name"))));
    }

    // ── scenario 8: returning user, blank fields — profile claims backfilled ──

    @Test
    public void testReturningUserBackfillsProfileFieldsWhenBlank() {
        final var uid = randomId();
        final var userId = randomId();
        final var existing = existingUser(userId);

        when(userUidDao.findUserUid(uid, SCHEME_NAME)).thenReturn(Optional.of(uid(uid, SCHEME_NAME, userId)));
        when(userDao.getUser(userId)).thenReturn(existing);

        assertNotNull(sessionWithProfileClaims(uid, null, "Pat", "Doe", "patdoe"));

        verify(userDao).updateUserStrict(argThat(u ->
                "patdoe".equals(u.getDisplayName())
                        && "Pat".equals(u.getFirstName())
                        && "Doe".equals(u.getLastName())
                        && u.getLinkedAccountProfiles() != null
                        && u.getLinkedAccountProfiles().containsKey(SCHEME_NAME)));
    }

    // ── scenario 9: returning user, already-set fields — never overwritten ────
    //   fill-only-if-blank: an existing value (admin-set, user-set, or from an earlier login) always wins over
    //   whatever a linked provider reports. linkedAccountProfiles is unaffected by this rule — it's a tracking
    //   snapshot, not a "don't overwrite" convenience field, so it still gets the latest value either way.

    @Test
    public void testReturningUserDoesNotOverwriteExistingProfileFields() {
        final var uid = randomId();
        final var userId = randomId();
        final var existing = existingUser(userId);
        existing.setFirstName("Custom");

        when(userUidDao.findUserUid(uid, SCHEME_NAME)).thenReturn(Optional.of(uid(uid, SCHEME_NAME, userId)));
        when(userDao.getUser(userId)).thenReturn(existing);

        assertNotNull(sessionWithProfileClaims(uid, null, "Pat", null, null));

        verify(userDao).updateUserStrict(argThat(u ->
                "Custom".equals(u.getFirstName())
                        && u.getLinkedAccountProfiles() != null
                        && "Pat".equals(u.getLinkedAccountProfiles().get(SCHEME_NAME).get("given_name"))));
    }

    // ── scenario 9b: backfill persistence failure must not block login ────────
    //   a pre-existing data issue on the user record (e.g. a legacy field value that predates a validation rule
    //   tightened since the account was created) can make updateUserStrict throw when persisting the backfilled
    //   profile claims. This is a best-effort capture, not part of the authentication contract — login must
    //   still succeed even if it fails.

    @Test
    public void testLoginSucceedsEvenWhenBackfillPersistenceFails() {
        final var uid = randomId();
        final var userId = randomId();
        final var existing = existingUser(userId);

        when(userUidDao.findUserUid(uid, SCHEME_NAME)).thenReturn(Optional.of(uid(uid, SCHEME_NAME, userId)));
        when(userDao.getUser(userId)).thenReturn(existing);
        // Simulates the real-world case: MongoUserDao.validate() rejects the full User object during
        // updateUserStrict because of an unrelated, pre-existing invalid field (e.g. 'name').
        when(userDao.updateUserStrict(any())).thenThrow(new RuntimeException("name is invalid"));

        final var result = sessionWithProfileClaims(uid, null, "Pat", null, null);

        assertNotNull(result, "Login must succeed even if the opportunistic profile backfill fails to persist");
        assertEquals(result.getSession().getUser().getId(), userId);
    }

    // ── scenario 10: linkedAccountProfiles preserved independently per scheme ─
    //   linking a second scheme to the same user must not clobber or remove the first scheme's entry.

    @Test
    public void testLinkedAccountProfilesPreservedIndependentlyAcrossSchemes() {
        final var userId = randomId();
        final var sub1 = randomId();
        final var sub2 = randomId();
        final var existing = existingUser(userId);

        when(userUidDao.findUserUid(sub1, SCHEME_NAME)).thenReturn(Optional.of(uid(sub1, SCHEME_NAME, userId)));
        when(userDao.getUser(userId)).thenReturn(existing);

        assertNotNull(sessionWithProfileClaims(sub1, null, "Pat", null, null, ISSUER, algorithm, KID));
        assertEquals(existing.getLinkedAccountProfiles().get(SCHEME_NAME).get("given_name"), "Pat");

        when(userUidDao.findUserUid(sub2, SCHEME_NAME_2)).thenReturn(Optional.of(uid(sub2, SCHEME_NAME_2, userId)));

        assertNotNull(sessionWithProfileClaims(sub2, null, null, "Smith", null, ISSUER_2, algorithm2, KID_2));

        assertEquals(existing.getLinkedAccountProfiles().get(SCHEME_NAME).get("given_name"), "Pat");
        assertEquals(existing.getLinkedAccountProfiles().get(SCHEME_NAME_2).get("family_name"), "Smith");
    }

    // ── scenario 11: same sub via two different providers (issuers) ────────────
    //   the ticket requires account linking to key on (issuer, sub), never sub
    //   alone — presenting the identical 'sub' value through two distinct
    //   provider schemes must therefore produce two distinct UserUid links,
    //   not be treated as the same identity.

    @Test
    public void testSameSubAcrossTwoProvidersProducesTwoDistinctLinks() {

        final var sharedSub = randomId();
        final var userIdFromProvider1 = randomId();
        final var userIdFromProvider2 = randomId();

        when(userUidDao.findUserUid(sharedSub, SCHEME_NAME)).thenReturn(Optional.empty());
        when(userUidDao.findUserUid(sharedSub, SCHEME_NAME_2)).thenReturn(Optional.empty());

        // Two distinct builder instances so each provider's creation flow can be verified independently
        // (a single shared mock couldn't distinguish which uid() call belongs to which create() call).
        final var creation1 = mock(UserCreation.class, RETURNS_SELF);
        final var creation2 = mock(UserCreation.class, RETURNS_SELF);
        when(userDao.newEmptyUser()).thenReturn(creation1, creation2);
        when(creation1.create()).thenReturn(user(new User(), userIdFromProvider1));
        when(creation2.create()).thenReturn(user(new User(), userIdFromProvider2));

        final var firstSession = session(sharedSub, null, ISSUER, algorithm, KID);
        final var secondSession = session(sharedSub, null, ISSUER_2, algorithm2, KID_2);

        assertNotNull(firstSession);
        assertNotNull(secondSession);
        assertNotEquals(
                firstSession.getSession().getUser().getId(),
                secondSession.getSession().getUser().getId(),
                "The same 'sub' from two different issuers must resolve to two distinct users/links");

        verify(creation1).uid(SCHEME_NAME, sharedSub, VerificationStatus.VERIFIED);
        verify(creation2).uid(SCHEME_NAME_2, sharedSub, VerificationStatus.VERIFIED);

    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private SessionCreation session(final String uid, final String email) {
        return session(uid, email, ISSUER, algorithm, KID);
    }

    private SessionCreation sessionWithProfileClaims(final String uid, final String email,
                                                      final String givenName, final String familyName,
                                                      final String preferredUsername) {
        return sessionWithProfileClaims(uid, email, givenName, familyName, preferredUsername, ISSUER, algorithm, KID);
    }

    private SessionCreation sessionWithProfileClaims(final String uid, final String email,
                                                      final String givenName, final String familyName,
                                                      final String preferredUsername,
                                                      final String issuer, final Algorithm signingAlgorithm,
                                                      final String kid) {
        var builder = JWT.create()
                .withIssuer(issuer)
                .withSubject(uid)
                .withKeyId(kid)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000));

        if (email != null) {
            builder = builder.withClaim("email", email).withClaim("email_verified", true);
        }
        if (givenName != null) {
            builder = builder.withClaim("given_name", givenName);
        }
        if (familyName != null) {
            builder = builder.withClaim("family_name", familyName);
        }
        if (preferredUsername != null) {
            builder = builder.withClaim("preferred_username", preferredUsername);
        }

        final var request = new OidcSessionRequest();
        request.setJwt(builder.sign(signingAlgorithm));
        return anonServiceProvider.get().createSession(request);
    }

    private SessionCreation session(final String uid, final String email,
                                     final String issuer, final Algorithm signingAlgorithm, final String kid) {
        var builder = JWT.create()
                .withIssuer(issuer)
                .withSubject(uid)
                .withKeyId(kid)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000));

        if (email != null) {
            builder = builder
                    .withClaim("email", email)
                    .withClaim("email_verified", true);
        }

        final var request = new OidcSessionRequest();
        request.setJwt(builder.sign(signingAlgorithm));
        return anonServiceProvider.get().createSession(request);
    }

    private OidcAuthScheme buildScheme() {
        final var n   = Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray());
        final var e   = Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray());
        final var jwk = new JWK("RS256", KID, "RSA", "sig", e, n);

        final var scheme = new OidcAuthScheme();
        scheme.setName(SCHEME_NAME);
        scheme.setIssuer(ISSUER);
        scheme.setKeys(List.of(jwk));
        return scheme;
    }

    private OidcAuthScheme buildScheme2() {
        final var n   = Base64.getUrlEncoder().encodeToString(publicKey2.getModulus().toByteArray());
        final var e   = Base64.getUrlEncoder().encodeToString(publicKey2.getPublicExponent().toByteArray());
        final var jwk = new JWK("RS256", KID_2, "RSA", "sig", e, n);

        final var scheme = new OidcAuthScheme();
        scheme.setName(SCHEME_NAME_2);
        scheme.setIssuer(ISSUER_2);
        scheme.setKeys(List.of(jwk));
        return scheme;
    }

    private static String randomId() {
        return UUID.randomUUID().toString();
    }

    private static User existingUser(final String id) {
        final var u = new User();
        u.setId(id);
        u.setLevel(USER);
        return u;
    }

    private static User user(final User proto, final String id) {
        proto.setId(id);
        return proto;
    }

    private static UserUid uid(final String id, final String scheme, final String userId) {
        final var u = new UserUid();
        u.setId(id);
        u.setScheme(scheme);
        u.setUserId(userId);
        return u;
    }

    // ── Guice test module ─────────────────────────────────────────────────────

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Client.class)             .toInstance(mock(Client.class));
            bind(ProfileDao.class)         .toInstance(mock(ProfileDao.class));
            bind(NameService.class)        .toInstance(mock(NameService.class));
            bind(ApplicationDao.class)     .toInstance(mock(ApplicationDao.class));
            bind(UserDao.class)            .toInstance(mock(UserDao.class));
            bind(UserUidDao.class)         .toInstance(mock(UserUidDao.class));
            bind(SessionDao.class)         .toInstance(mock(SessionDao.class));
            bind(OidcAuthSchemeDao.class)  .toInstance(mock(OidcAuthSchemeDao.class));
            bind(CryptoKeyPairUtility.class).toInstance(mock(CryptoKeyPairUtility.class));
            bind(OidcAuthSchemeService.class).toInstance(mock(OidcAuthSchemeService.class));
            bind(ElementRegistry.class).toInstance(mock(ElementRegistry.class));

            bind(MapperRegistry.class).toProvider(ServicesMapperRegistryProvider.class);
            bind(long.class)  .annotatedWith(named(SESSION_TIMEOUT_SECONDS)).toInstance(300L);
            bind(long.class)  .annotatedWith(named(OIDC_JWKS_REFRESH_SECONDS)).toInstance(3600L);
            bind(String.class).annotatedWith(named(API_OUTSIDE_URL)).toInstance("http://localhost:8080/api/rest");
        }
    }
}
