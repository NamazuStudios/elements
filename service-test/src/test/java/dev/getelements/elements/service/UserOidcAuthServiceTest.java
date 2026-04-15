package dev.getelements.elements.service;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.exception.auth.AuthValidationException;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.service.auth.oidc.OidcAuthServiceOperations;
import dev.getelements.elements.service.auth.oidc.UserOidcAuthService;
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

public class UserOidcAuthServiceTest {

    private static final String SCHEME_NAME = "UserOidcTestScheme";
    private static final String SUB = "sub-ext-user";
    private static final String EMAIL = "bob@example.com";
    private static final String CURRENT_USER_ID = "current-user-id";

    @Inject private UserOidcAuthService service;
    @Inject private UserUidDao userUidDao;
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
     * with the supplied jwt and scheme.
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
     * New UID → links sub UID to current user; no new user created.
     */
    @Test
    public void testNewUid_linksSubToCurrentUser() {
        when(userUidDao.findUserUid(SUB, SCHEME_NAME)).thenReturn(Optional.empty());
        when(userUidDao.findUserUid(EMAIL, UserUidDao.SCHEME_EMAIL)).thenReturn(Optional.empty());

        runMapper(jwt(SUB, EMAIL, true), scheme(SCHEME_NAME));

        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao, atLeastOnce()).createUserUidStrict(uidCaptor.capture());

        final var subUid = uidCaptor.getAllValues().stream()
                .filter(u -> SUB.equals(u.getId()) && SCHEME_NAME.equals(u.getScheme()))
                .findFirst()
                .orElse(null);
        assertNotNull(subUid, "Sub UID should be created");
        assertEquals(subUid.getUserId(), CURRENT_USER_ID);

        final var sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertEquals(sessionCaptor.getValue().getUser().getId(), CURRENT_USER_ID);
    }

    /**
     * OIDC sub already linked to the same user → succeeds (idempotent), no new UID created.
     */
    @Test
    public void testSubAlreadyLinkedToSameUser_idempotent() {
        when(userUidDao.findUserUid(SUB, SCHEME_NAME))
                .thenReturn(Optional.of(uidFor(SUB, SCHEME_NAME, CURRENT_USER_ID)));

        runMapper(jwt(SUB, null, false), scheme(SCHEME_NAME));

        verify(userUidDao, never()).createUserUidStrict(any());

        final var sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertEquals(sessionCaptor.getValue().getUser().getId(), CURRENT_USER_ID);
    }

    /**
     * OIDC sub already linked to a different user → throws AuthValidationException.
     */
    @Test
    public void testSubAlreadyLinkedToDifferentUser_throwsException() {
        when(userUidDao.findUserUid(SUB, SCHEME_NAME))
                .thenReturn(Optional.of(uidFor(SUB, SCHEME_NAME, "other-user-id")));

        assertThrows(AuthValidationException.class,
                () -> runMapper(jwt(SUB, null, false), scheme(SCHEME_NAME)));

        verify(sessionDao, never()).create(any());
        verify(userUidDao, never()).createUserUidStrict(any());
    }

    /**
     * Verified email not yet linked → email UID created and linked to current user.
     */
    @Test
    public void testVerifiedEmail_notLinked_createsEmailUid() {
        when(userUidDao.findUserUid(SUB, SCHEME_NAME)).thenReturn(Optional.empty());
        when(userUidDao.findUserUid(EMAIL, UserUidDao.SCHEME_EMAIL)).thenReturn(Optional.empty());

        runMapper(jwt(SUB, EMAIL, true), scheme(SCHEME_NAME));

        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao, times(2)).createUserUidStrict(uidCaptor.capture());

        final var emailUid = uidCaptor.getAllValues().stream()
                .filter(u -> EMAIL.equals(u.getId()) && UserUidDao.SCHEME_EMAIL.equals(u.getScheme()))
                .findFirst()
                .orElse(null);
        assertNotNull(emailUid, "Email UID should be created for verified email");
        assertEquals(emailUid.getUserId(), CURRENT_USER_ID);
    }

    /**
     * Unverified email → email UID NOT created.
     */
    @Test
    public void testUnverifiedEmail_emailUidNotCreated() {
        when(userUidDao.findUserUid(SUB, SCHEME_NAME)).thenReturn(Optional.empty());

        runMapper(jwt(SUB, EMAIL, false), scheme(SCHEME_NAME));

        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao, times(1)).createUserUidStrict(uidCaptor.capture());
        assertEquals(uidCaptor.getValue().getId(), SUB);
        assertEquals(uidCaptor.getValue().getScheme(), SCHEME_NAME);

        // No email UID lookup should be performed at all
        verify(userUidDao, never()).findUserUid(EMAIL, UserUidDao.SCHEME_EMAIL);
    }

    /**
     * Verified email linked to a different user → email UID skipped (no exception), sub UID still linked.
     */
    @Test
    public void testVerifiedEmail_linkedToDifferentUser_skipsEmailUid() {
        when(userUidDao.findUserUid(SUB, SCHEME_NAME)).thenReturn(Optional.empty());
        when(userUidDao.findUserUid(EMAIL, UserUidDao.SCHEME_EMAIL))
                .thenReturn(Optional.of(uidFor(EMAIL, UserUidDao.SCHEME_EMAIL, "other-user-id")));

        // Must not throw — email conflict is a soft skip
        runMapper(jwt(SUB, EMAIL, true), scheme(SCHEME_NAME));

        // Sub UID still linked, email UID not duplicated
        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao, times(1)).createUserUidStrict(uidCaptor.capture());
        assertEquals(uidCaptor.getValue().getId(), SUB);
        assertEquals(uidCaptor.getValue().getScheme(), SCHEME_NAME);
        assertEquals(uidCaptor.getValue().getUserId(), CURRENT_USER_ID);
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
            final var currentUser = new User();
            currentUser.setId(CURRENT_USER_ID);
            bind(User.class).toInstance(currentUser);

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
