package dev.getelements.elements.service.auth.oidc.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.auth.OidcProviderGrant;
import dev.getelements.elements.sdk.model.auth.SessionTokenClaims;
import dev.getelements.elements.sdk.model.exception.auth.OAuth2Exception;
import dev.getelements.elements.sdk.model.oidc.OidcProviderUserInfo;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import dev.getelements.elements.sdk.service.auth.SessionTokenVerifier;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_REQUEST;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;

/**
 * Unit tests for the userinfo claim resolution. Claims are gated by the scopes recorded on the grant backing
 * the presented access token; tokens not issued by this provider must be refused outright.
 */
public class StandardOidcProviderUserServiceTest {

    private OidcProviderDao oidcProviderDao;

    private UserDao userDao;

    private UserUidDao userUidDao;

    private SessionTokenVerifier sessionTokenVerifier;

    @Inject
    private StandardOidcProviderUserService service;

    private User user;

    @BeforeMethod
    public void setup() {

        oidcProviderDao = mock(OidcProviderDao.class);
        userDao = mock(UserDao.class);
        userUidDao = mock(UserUidDao.class);
        sessionTokenVerifier = mock(SessionTokenVerifier.class);

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(OidcProviderDao.class).toInstance(oidcProviderDao);
                bind(UserDao.class).toInstance(userDao);
                bind(UserUidDao.class).toInstance(userUidDao);
                bind(SessionTokenVerifier.class).toInstance(sessionTokenVerifier);
                bind(StandardOidcProviderUserService.class);
            }
        }).injectMembers(this);

        user = new User();
        user.setId("user-1");
        user.setName("testuser");
        user.setDisplayName("Test User");
        user.setEmail("user@example.com");

        when(userDao.getUser("user-1")).thenReturn(user);

        final var claims = new SessionTokenClaims();
        claims.setSubject("user-1");
        claims.setSessionId("sid-1");
        when(sessionTokenVerifier.verify(anyString())).thenReturn(claims);

    }

    private void grantWithScope(final String scope) {
        final var grant = new OidcProviderGrant();
        grant.setId("grant-1");
        grant.setClientId("el_test");
        grant.setUserId("user-1");
        grant.setScope(scope);
        grant.setSessionId("sid-1");
        when(oidcProviderDao.findGrantBySessionId("sid-1")).thenReturn(Optional.of(grant));
    }

    private void verifiedEmail(final boolean verified) {
        final var userUid = new UserUid();
        userUid.setScheme(UserUidDao.SCHEME_EMAIL);
        userUid.setVerificationStatus(verified ? VerificationStatus.VERIFIED : VerificationStatus.UNVERIFIED);
        when(userUidDao.findUserUid("user@example.com", UserUidDao.SCHEME_EMAIL))
                .thenReturn(Optional.of(userUid));
    }

    @Test
    public void testUserInfoReturnsAlwaysPresentSub() {
        grantWithScope("openid");
        final var userInfo = service.getUserInfo("token");
        assertEquals(userInfo.getSub(), "user-1");
        assertNull(userInfo.getName());
        assertNull(userInfo.getPreferredUsername());
        assertNull(userInfo.getEmail());
    }

    @Test
    public void testUserInfoProfileScopeGatesProfileClaims() {
        grantWithScope("openid profile");
        final var userInfo = service.getUserInfo("token");
        assertEquals(userInfo.getName(), "Test User");
        assertEquals(userInfo.getPreferredUsername(), "testuser");
        assertNull(userInfo.getEmail());
    }

    @Test
    public void testUserInfoEmailScopeGatesEmailClaims() {

        grantWithScope("openid email");
        verifiedEmail(true);

        final var userInfo = service.getUserInfo("token");

        assertEquals(userInfo.getEmail(), "user@example.com");
        assertEquals(userInfo.getEmailVerified(), Boolean.TRUE);
        assertNull(userInfo.getName());

    }

    @Test
    public void testUserInfoEmailVerifiedReflectsVerificationStatus() {

        grantWithScope("openid email");

        verifiedEmail(false);
        assertEquals(service.getUserInfo("token").getEmailVerified(), Boolean.FALSE);

        when(userUidDao.findUserUid("user@example.com", UserUidDao.SCHEME_EMAIL)).thenReturn(Optional.empty());
        assertEquals(service.getUserInfo("token").getEmailVerified(), Boolean.FALSE);

    }

    @Test
    public void testUserInfoOmitsEmailClaimsWhenEmailScopeAbsent() {
        grantWithScope("openid profile");
        final var userInfo = service.getUserInfo("token");
        assertNull(userInfo.getEmail());
        assertNull(userInfo.getEmailVerified());
    }

    @Test
    public void testUserInfoRefusesTokensWithoutAProviderGrant() {
        // The access token is a valid Elements session token, but this provider issued no grant for its
        // session: it did not come from this provider and must not resolve to claims.
        when(oidcProviderDao.findGrantBySessionId("sid-1")).thenReturn(Optional.empty());
        assertThrows(dev.getelements.elements.sdk.model.exception.UnauthorizedException.class,
                () -> service.getUserInfo("token"));
    }

    @Test
    public void testUserInfoRefusesTokensForDeletedUsers() {
        grantWithScope("openid profile");
        when(userDao.getUser("user-1")).thenReturn(null);
        assertThrows(dev.getelements.elements.sdk.model.exception.UnauthorizedException.class,
                () -> service.getUserInfo("token"));
    }

    public StandardOidcProviderUserService getService() {
        return service;
    }

}
