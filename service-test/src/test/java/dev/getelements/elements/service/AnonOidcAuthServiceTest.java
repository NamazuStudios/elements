package dev.getelements.elements.service;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.service.auth.oidc.AnonOidcAuthService;
import dev.getelements.elements.service.auth.oidc.OidcAuthServiceOperations;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.function.BiFunction;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class AnonOidcAuthServiceTest {

    private static final String SCHEME_NAME = "AnonOidcTestScheme";
    private static final String SUB_1 = "sub-user-1";
    private static final String SUB_2 = "sub-user-2";
    private static final String EMAIL = "alice@example.com";

    @Inject private AnonOidcAuthService service;
    @Inject private UserUidDao userUidDao;
    @Inject private UserDao userDao;
    @Inject private OidcAuthServiceOperations oidcAuthServiceOperations;
    @Inject private SessionDao sessionDao;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
        when(sessionDao.create(any(Session.class))).thenReturn(new SessionCreation());
        when(userUidDao.createUserUidStrict(any(UserUid.class))).then(i -> i.getArgument(0));
    }

    /**
     * Configures the mocked OidcAuthServiceOperations to invoke the service's BiFunction
     * with the supplied jwt and scheme, then captures the returned user in a Session.
     */
    private void runMapper(DecodedJWT jwt, OidcAuthScheme scheme) {
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            final BiFunction<DecodedJWT, OidcAuthScheme, User> mapper = invocation.getArgument(1);
            final var user = mapper.apply(jwt, scheme);
            final var session = new Session();
            session.setUser(user);
            return sessionDao.create(session);
        }).when(oidcAuthServiceOperations).createOrUpdateUserWithToken(any(), any());

        service.createSession(new OidcSessionRequest());
    }

    /**
     * New OIDC user, email_verified=true → creates user with email set, no name set, both sub+email UIDs created.
     */
    @Test
    public void testNewUser_emailVerified_createsUserWithEmailAndBothUids() {
        when(userUidDao.findUserUid(SUB_1, SCHEME_NAME)).thenReturn(Optional.empty());
        when(userUidDao.findUserUid(EMAIL, UserUidDao.SCHEME_EMAIL)).thenReturn(Optional.empty());

        final var newUser = userWithId("new-user-1");
        when(userDao.createUserStrict(any(User.class))).thenReturn(newUser);

        runMapper(jwt(SUB_1, EMAIL, true), scheme(SCHEME_NAME));

        final var userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).createUserStrict(userCaptor.capture());
        verify(userDao, never()).createUser(any());
        assertEquals(userCaptor.getValue().getEmail(), EMAIL);
        assertNull(userCaptor.getValue().getName(), "Name must not be copied from email");

        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao, times(2)).createUserUidStrict(uidCaptor.capture());
        final var uids = uidCaptor.getAllValues();
        assertTrue(uids.stream().anyMatch(u -> SUB_1.equals(u.getId()) && SCHEME_NAME.equals(u.getScheme())));
        assertTrue(uids.stream().anyMatch(u -> EMAIL.equals(u.getId()) && UserUidDao.SCHEME_EMAIL.equals(u.getScheme())));
    }

    /**
     * New OIDC user, email_verified=false → creates user without email, only sub UID created.
     */
    @Test
    public void testNewUser_emailNotVerified_createsUserWithoutEmailUid() {
        when(userUidDao.findUserUid(SUB_1, SCHEME_NAME)).thenReturn(Optional.empty());

        final var newUser = userWithId("new-user-2");
        when(userDao.createUserStrict(any(User.class))).thenReturn(newUser);

        runMapper(jwt(SUB_1, EMAIL, false), scheme(SCHEME_NAME));

        final var userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).createUserStrict(userCaptor.capture());
        assertNull(userCaptor.getValue().getEmail(), "Email must not be set when not verified");

        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao, times(1)).createUserUidStrict(uidCaptor.capture());
        assertEquals(uidCaptor.getValue().getId(), SUB_1);
        assertEquals(uidCaptor.getValue().getScheme(), SCHEME_NAME);
    }

    /**
     * Existing user found by sub UID → returns that user, no new user created.
     */
    @Test
    public void testExistingUser_foundBySubUid_returnsExistingUser() {
        when(userUidDao.findUserUid(SUB_1, SCHEME_NAME))
                .thenReturn(Optional.of(uidFor(SUB_1, SCHEME_NAME, "existing-user-3")));
        when(userUidDao.findUserUid(EMAIL, UserUidDao.SCHEME_EMAIL)).thenReturn(Optional.empty());
        when(userDao.getUser("existing-user-3")).thenReturn(userWithId("existing-user-3"));

        runMapper(jwt(SUB_1, EMAIL, true), scheme(SCHEME_NAME));

        verify(userDao, never()).createUserStrict(any());
        verify(userDao, never()).createUser(any());

        // email UID not yet present → should be created
        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao, times(1)).createUserUidStrict(uidCaptor.capture());
        assertEquals(uidCaptor.getValue().getId(), EMAIL);
        assertEquals(uidCaptor.getValue().getScheme(), UserUidDao.SCHEME_EMAIL);
        assertEquals(uidCaptor.getValue().getUserId(), "existing-user-3");
    }

    /**
     * Existing user found only by email UID (verified) → sub UID linked to that user.
     */
    @Test
    public void testExistingUser_foundByEmailUid_linksSubToEmailUser() {
        when(userUidDao.findUserUid(SUB_1, SCHEME_NAME)).thenReturn(Optional.empty());
        when(userUidDao.findUserUid(EMAIL, UserUidDao.SCHEME_EMAIL))
                .thenReturn(Optional.of(uidFor(EMAIL, UserUidDao.SCHEME_EMAIL, "existing-user-4")));
        when(userDao.getUser("existing-user-4")).thenReturn(userWithId("existing-user-4"));

        runMapper(jwt(SUB_1, EMAIL, true), scheme(SCHEME_NAME));

        verify(userDao, never()).createUserStrict(any());

        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao, times(1)).createUserUidStrict(uidCaptor.capture());
        assertEquals(uidCaptor.getValue().getId(), SUB_1);
        assertEquals(uidCaptor.getValue().getScheme(), SCHEME_NAME);
        assertEquals(uidCaptor.getValue().getUserId(), "existing-user-4");
    }

    /**
     * Two distinct subs without email → each gets their own new user (regression for createUserStrict).
     */
    @Test
    public void testTwoDistinctSubs_createDistinctUsers() {
        when(userUidDao.findUserUid(SUB_1, SCHEME_NAME)).thenReturn(Optional.empty());
        when(userUidDao.findUserUid(SUB_2, SCHEME_NAME)).thenReturn(Optional.empty());

        when(userDao.createUserStrict(any(User.class)))
                .thenReturn(userWithId("user-a"))
                .thenReturn(userWithId("user-b"));

        runMapper(jwt(SUB_1, null, false), scheme(SCHEME_NAME));
        runMapper(jwt(SUB_2, null, false), scheme(SCHEME_NAME));

        verify(userDao, times(2)).createUserStrict(any(User.class));
        verify(userDao, never()).createUser(any());

        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao, times(2)).createUserUidStrict(uidCaptor.capture());
        final var uids = uidCaptor.getAllValues();
        assertNotEquals(uids.get(0).getUserId(), uids.get(1).getUserId(),
                "Different external subs must produce different Elements users");
    }

    // ---------- helpers ----------

    private static OidcAuthScheme scheme(String name) {
        final var s = new OidcAuthScheme();
        s.setName(name);
        return s;
    }

    private static DecodedJWT jwt(String sub, String email, boolean emailVerified) {
        // Pre-create all Claim mocks before any when() to avoid UnfinishedStubbing errors
        final var subClaim = stringClaim(sub);
        final var emailClaim = stringClaim(email);
        final var evClaim = mock(Claim.class);
        when(evClaim.asBoolean()).thenReturn(emailVerified);

        final var jwt = mock(DecodedJWT.class);
        when(jwt.getClaim("sub")).thenReturn(subClaim);
        when(jwt.getClaim("email")).thenReturn(emailClaim);
        when(jwt.getClaim("email_verified")).thenReturn(evClaim);
        return jwt;
    }

    private static Claim stringClaim(String value) {
        final var c = mock(Claim.class);
        when(c.asString()).thenReturn(value);
        return c;
    }

    private static User userWithId(String id) {
        final var u = new User();
        u.setId(id);
        return u;
    }

    private static UserUid uidFor(String id, String scheme, String userId) {
        final var u = new UserUid();
        u.setId(id);
        u.setScheme(scheme);
        u.setUserId(userId);
        return u;
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserUidDao.class).toInstance(mock(UserUidDao.class));
            bind(UserDao.class).toInstance(mock(UserDao.class));
            bind(SessionDao.class).toInstance(mock(SessionDao.class));
            bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
            bind(NameService.class).toInstance(mock(NameService.class));
            bind(ApplicationDao.class).toInstance(mock(ApplicationDao.class));
            bind(Client.class).toInstance(mock(Client.class));
            bind(OidcAuthSchemeDao.class).toInstance(mock(OidcAuthSchemeDao.class));
            bind(OidcAuthServiceOperations.class).toInstance(mock(OidcAuthServiceOperations.class));
            bind(ElementRegistry.class).toInstance(mock(ElementRegistry.class));
            bindConstant().annotatedWith(named(SESSION_TIMEOUT_SECONDS)).to(3600L);
            bind(String.class).annotatedWith(named(API_OUTSIDE_URL)).toInstance("http://localhost:8080/api/rest");
        }
    }
}
