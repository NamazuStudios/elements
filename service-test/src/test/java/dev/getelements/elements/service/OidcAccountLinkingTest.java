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

    private RSAPublicKey publicKey;
    private Algorithm   algorithm;

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

    @BeforeClass
    public void setupKeys() throws Exception {
        final var kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        final var kp  = kpg.generateKeyPair();
        publicKey  = (RSAPublicKey) kp.getPublic();
        algorithm  = Algorithm.RSA256(publicKey, (RSAPrivateKey) kp.getPrivate());
    }

    @BeforeClass(dependsOnMethods = "setupKeys")
    public void setupInjector() {
        createInjector(new TestModule()).injectMembers(this);
        when(oidcAuthSchemeDao.findAuthScheme(ISSUER)).thenReturn(Optional.of(buildScheme()));
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
    }

    // ── scenario 1: brand-new user ────────────────────────────────────────────

    @Test
    public void testNewUserCreatesOidcAndEmailUid() {
        final var uid   = randomId();
        final var email = "new@example.com";
        final var newId = randomId();

        when(userUidDao.findUserUid(uid,   SCHEME_NAME))              .thenReturn(Optional.empty());
        when(userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL))  .thenReturn(Optional.empty());
        when(userDao.createUserStrict(any())).then(i -> user(i.getArgument(0), newId));

        assertNotNull(session(uid, email));

        verify(userDao).createUserStrict(any());
        verify(userUidDao).createUserUidStrict(argThat(u -> SCHEME_NAME.equals(u.getScheme())             && uid.equals(u.getId())));
        verify(userUidDao).createUserUidStrict(argThat(u -> UserUidDao.SCHEME_EMAIL.equals(u.getScheme()) && email.equals(u.getId())));
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

    // ── scenario 4: stale email UID (user was deleted) ────────────────────────
    //   email UID document exists but its userId is null; code must delete the
    //   stale UID and relink it to the newly-created user

    @Test
    public void testStaleEmailUidIsDeletedAndRelinkedToNewUser() {
        final var uid      = randomId();
        final var email    = "stale@example.com";
        final var newUserId = randomId();

        final var staleEmailUid = uid(email, UserUidDao.SCHEME_EMAIL, null);
        when(userUidDao.findUserUid(uid,   SCHEME_NAME))             .thenReturn(Optional.empty());
        when(userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL)) .thenReturn(Optional.of(staleEmailUid));
        when(userDao.createUserStrict(any())).then(i -> user(i.getArgument(0), newUserId));

        assertNotNull(session(uid, email));

        verify(userDao).createUserStrict(any());
        verify(userUidDao).tryDeleteUserUid(staleEmailUid);
        verify(userUidDao).createUserUidStrict(argThat(u -> UserUidDao.SCHEME_EMAIL.equals(u.getScheme()) && email.equals(u.getId())));
    }

    // ── scenario 5: JWT has no email claim ────────────────────────────────────

    @Test
    public void testNullEmailSkipsEmailUidLookupAndCreation() {
        final var uid      = randomId();
        final var newUserId = randomId();

        when(userUidDao.findUserUid(uid, SCHEME_NAME)).thenReturn(Optional.empty());
        when(userDao.createUserStrict(any())).then(i -> user(i.getArgument(0), newUserId));

        assertNotNull(session(uid, null));

        verify(userDao).createUserStrict(any());
        verify(userUidDao).createUserUidStrict(argThat(u -> SCHEME_NAME.equals(u.getScheme()) && uid.equals(u.getId())));
        verify(userUidDao, never()).findUserUid(anyString(), eq(UserUidDao.SCHEME_EMAIL));
        verify(userUidDao, never()).createUserUidStrict(argThat(u -> UserUidDao.SCHEME_EMAIL.equals(u.getScheme())));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private SessionCreation session(final String uid, final String email) {
        var builder = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(uid)
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000));

        if (email != null) {
            builder = builder
                    .withClaim("email", email)
                    .withClaim("email_verified", true);
        }

        final var request = new OidcSessionRequest();
        request.setJwt(builder.sign(algorithm));
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
            bind(String.class).annotatedWith(named(API_OUTSIDE_URL)).toInstance("http://localhost:8080/api/rest");
        }
    }
}
