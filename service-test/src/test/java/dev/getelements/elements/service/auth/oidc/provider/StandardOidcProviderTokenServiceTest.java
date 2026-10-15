package dev.getelements.elements.service.auth.oidc.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.SignedJWT;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.auth.OidcProviderGrant;
import dev.getelements.elements.sdk.model.auth.OidcRefreshToken;
import dev.getelements.elements.sdk.model.auth.SessionTokenClaims;
import dev.getelements.elements.sdk.model.exception.auth.OAuth2Exception;
import dev.getelements.elements.sdk.model.oidc.OidcProviderTokenRequest;
import dev.getelements.elements.sdk.model.oidc.OidcProviderTokenResponse;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.Constants;
import dev.getelements.elements.sdk.service.auth.SessionTokenIssuer;
import dev.getelements.elements.sdk.service.auth.SessionTokenVerifier;
import dev.getelements.elements.service.auth.StandardSessionTokenKeySupport;
import jakarta.inject.Inject;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.nimbusds.jose.util.Base64URL.encode;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_CLIENT;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_GRANT;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_SCOPE;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_TARGET;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_UNAUTHORIZED_CLIENT;
import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static java.lang.System.currentTimeMillis;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class StandardOidcProviderTokenServiceTest {

    private static final String CLIENT_ID = "el_test";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String REDIRECT_URI = "https://client.example.com/callback";
    private static final String VERIFIER = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";

    private OidcProviderDao oidcProviderDao;

    private UserDao userDao;

    private ApplicationDao applicationDao;

    private ProfileDao profileDao;

    private SessionDao sessionDao;

    private SessionTokenIssuer sessionTokenIssuer;

    private SessionTokenVerifier sessionTokenVerifier;

    private StandardSessionTokenKeySupport sessionTokenKeySupport;

    @Inject
    private StandardOidcProviderTokenService service;

    private OidcClientRegistration registration;

    private RSAPublicKey publicKey;

    private RSAPrivateKey privateKey;

    private User user;

    @BeforeMethod
    public void setup() throws Exception {

        oidcProviderDao = mock(OidcProviderDao.class);
        userDao = mock(UserDao.class);
        applicationDao = mock(ApplicationDao.class);
        profileDao = mock(ProfileDao.class);
        sessionDao = mock(SessionDao.class);
        sessionTokenIssuer = mock(SessionTokenIssuer.class);
        sessionTokenVerifier = mock(SessionTokenVerifier.class);
        sessionTokenKeySupport = mock(StandardSessionTokenKeySupport.class);

        final var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        final var keyPair = keyPairGenerator.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        final var signingKey = new dev.getelements.elements.sdk.model.auth.JwtSigningKey();
        signingKey.setKid("kid-1");

        when(sessionTokenKeySupport.getActiveSigningKey()).thenReturn(signingKey);
        when(sessionTokenKeySupport.getPrivateKey(any())).thenReturn(privateKey);
        when(sessionTokenKeySupport.getPublicKey("kid-1")).thenReturn(publicKey);

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(OidcProviderDao.class).toInstance(oidcProviderDao);
                bind(UserDao.class).toInstance(userDao);
                bind(ApplicationDao.class).toInstance(applicationDao);
                bind(ProfileDao.class).toInstance(profileDao);
                bind(SessionDao.class).toInstance(sessionDao);
                bind(SessionTokenIssuer.class).toInstance(sessionTokenIssuer);
                bind(SessionTokenVerifier.class).toInstance(sessionTokenVerifier);
                bind(StandardSessionTokenKeySupport.class).toInstance(sessionTokenKeySupport);
                bind(OidcProviderRateLimiter.class);
                // The key support's own @Inject members are member-injected even on the mock, so its
                // dependencies must be present in the injector.
                bind(dev.getelements.elements.sdk.dao.JwtSigningKeyDao.class)
                        .toInstance(mock(dev.getelements.elements.sdk.dao.JwtSigningKeyDao.class));
                bind(String.class).annotatedWith(Names.named(Constants.SESSION_TOKEN_KEY_PASSPHRASE))
                        .toInstance("test-passphrase");
                bind(long.class).annotatedWith(Names.named(Constants.SESSION_TOKEN_KEY_REFRESH_SECONDS))
                        .toInstance(300L);
                bind(String.class).annotatedWith(Names.named(Constants.OIDC_PROVIDER_ISSUER))
                        .toInstance("https://elements.example.com/api/rest");
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_ID_TOKEN_TTL_SECONDS)).toInstance(3600L);
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_REFRESH_GRANT_TTL_SECONDS)).toInstance(2592000L);
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_MACHINE_TOKEN_TTL_SECONDS)).toInstance(300L);
                bind(long.class).annotatedWith(Names.named(Constants.SESSION_TIMEOUT_SECONDS)).toInstance(172800L);
                bind(boolean.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_ENABLED)).toInstance(false);
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_TOKEN_PER_MINUTE)).toInstance(60L);
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_AUTHORIZE_PER_MINUTE)).toInstance(60L);
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_LOGIN_PER_MINUTE)).toInstance(10L);
                bind(StandardOidcProviderTokenService.class);
            }
        }).injectMembers(this);

        registration = new OidcClientRegistration();
        registration.setClientId(CLIENT_ID);
        registration.setDisplayName("Test client");
        registration.setRedirectUris(Set.of(REDIRECT_URI));
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.AUTHORIZATION_CODE,
                OidcClientRegistration.GrantType.REFRESH_TOKEN));
        registration.setScopes(Set.of("openid", "profile", "email"));
        registration.setStatus(OidcClientRegistration.Status.ACTIVE);

        when(oidcProviderDao.getClient(CLIENT_ID)).thenReturn(Optional.of(registration));
        when(oidcProviderDao.verifyClientSecret(CLIENT_ID, CLIENT_SECRET)).thenReturn(true);

        user = new User();
        user.setId("user-1");
        user.setLevel(USER);
        user.setName("testuser");
        user.setDisplayName("Test User");
        user.setEmail("user@example.com");
        when(userDao.getUser("user-1")).thenReturn(user);

        final var session = new dev.getelements.elements.sdk.model.session.Session();
        session.setUser(user);
        session.setExpiry(currentTimeMillis() + 1_800_000);

        final var sessionCreation = new SessionCreation();
        sessionCreation.setSession(session);
        sessionCreation.setSessionSecret("access-token-value");

        when(sessionTokenIssuer.issue(any())).thenReturn(sessionCreation);

        final var claims = new SessionTokenClaims();
        claims.setSubject("user-1");
        claims.setSessionId("sid-1");
        when(sessionTokenVerifier.verify(anyString())).thenReturn(claims);

    }

    private OidcAuthorizationCode codeRecord() {
        final var code = new OidcAuthorizationCode();
        code.setCode("the-code");
        code.setClientId(CLIENT_ID);
        code.setUserId("user-1");
        code.setRedirectUri(REDIRECT_URI);
        code.setScope("openid profile");
        code.setNonce("nonce-123");
        code.setCodeChallengeMethod("S256");
        code.setCodeChallenge(pkceChallenge(VERIFIER));
        code.setAuthTime(currentTimeMillis() - 60_000);
        code.setStatus(OidcAuthorizationCode.Status.PENDING);
        code.setExpiry(new Timestamp(currentTimeMillis() + 600_000));
        return code;
    }

    private String pkceChallenge(final String verifier) {
        try {
            return encode(MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII))).toString();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private OidcProviderTokenRequest exchangeRequest() {
        final var request = new OidcProviderTokenRequest();
        request.setGrantType("authorization_code");
        request.setCode("the-code");
        request.setRedirectUri(REDIRECT_URI);
        request.setCodeVerifier(VERIFIER);
        request.setClientId(CLIENT_ID);
        request.setClientSecret(CLIENT_SECRET);
        return request;
    }

    @Test
    public void testExchangeCodeIssuesTokens() throws Exception {

        when(oidcProviderDao.redeemAuthorizationCode("the-code"))
                .thenReturn(Optional.of(codeRecord()));
        when(oidcProviderDao.createGrant(any())).then(i -> i.getArgument(0));
        when(oidcProviderDao.createRefreshToken(any())).then(i -> i.getArgument(0));

        final var response = getService().exchangeCode(exchangeRequest());

        assertEquals(response.getTokenType(), "Bearer");
        assertEquals(response.getAccessToken(), "access-token-value");
        assertNotNull(response.getRefreshToken());
        assertTrue(response.getExpiresIn() > 1700 && response.getExpiresIn() <= 1800);

        // The grant anchors the session and mirrors the code's authorization context.
        final var grantCaptor = ArgumentCaptor.forClass(OidcProviderGrant.class);
        verify(oidcProviderDao).createGrant(grantCaptor.capture());
        assertEquals(grantCaptor.getValue().getSessionId(), "sid-1");
        assertEquals(grantCaptor.getValue().getClientId(), CLIENT_ID);
        assertEquals(grantCaptor.getValue().getUserId(), "user-1");
        assertEquals(grantCaptor.getValue().getScope(), "openid profile");

        // The refresh token record carries only the digest, never the raw token.
        final var tokenCaptor = ArgumentCaptor.forClass(OidcRefreshToken.class);
        verify(oidcProviderDao).createRefreshToken(tokenCaptor.capture());
        assertTrue(tokenCaptor.getValue().getSecretHash().length() == 64);
        assertFalse(tokenCaptor.getValue().getSecretHash().contains(response.getRefreshToken()));

        // The id_token is signed by the shared key lifecycle and carries the OIDC claims.
        final var idToken = SignedJWT.parse(response.getIdToken());
        assertEquals(idToken.getHeader().getKeyID(), "kid-1");
        assertEquals(idToken.getHeader().getAlgorithm(), JWSAlgorithm.RS256);
        assertTrue(idToken.verify(new com.nimbusds.jose.crypto.RSASSAVerifier(publicKey)));
        assertEquals(idToken.getJWTClaimsSet().getIssuer(), "https://elements.example.com/api/rest");
        assertEquals(idToken.getJWTClaimsSet().getSubject(), "user-1");
        assertEquals(idToken.getJWTClaimsSet().getAudience().get(0), CLIENT_ID);
        assertEquals(idToken.getJWTClaimsSet().getStringClaim("azp"), CLIENT_ID);
        assertEquals(idToken.getJWTClaimsSet().getStringClaim("nonce"), "nonce-123");
        assertNotNull(idToken.getJWTClaimsSet().getClaim("auth_time"));

    }

    @Test
    public void testExchangeCodeRejectsPkceMismatch() {
        when(oidcProviderDao.redeemAuthorizationCode("the-code")).thenReturn(Optional.of(codeRecord()));
        final var request = exchangeRequest();
        request.setCodeVerifier("wrong-verifier");
        assertOAuth2Error(ERROR_INVALID_GRANT, () -> getService().exchangeCode(request));
    }

    @Test
    public void testExchangeCodeRejectsRedirectMismatch() {
        when(oidcProviderDao.redeemAuthorizationCode("the-code")).thenReturn(Optional.of(codeRecord()));
        final var request = exchangeRequest();
        request.setRedirectUri("https://client.example.com/other");
        assertOAuth2Error(ERROR_INVALID_GRANT, () -> getService().exchangeCode(request));
    }

    @Test
    public void testExchangeCodeRejectsClientMismatch() {
        final var code = codeRecord();
        code.setClientId("el_other");
        when(oidcProviderDao.redeemAuthorizationCode("the-code")).thenReturn(Optional.of(code));
        assertOAuth2Error(ERROR_INVALID_GRANT, () -> getService().exchangeCode(exchangeRequest()));
    }

    @Test
    public void testExchangeCodeRejectsReplayedCode() {
        when(oidcProviderDao.redeemAuthorizationCode("the-code")).thenReturn(Optional.empty());
        assertOAuth2Error(ERROR_INVALID_GRANT, () -> getService().exchangeCode(exchangeRequest()));
    }

    @Test
    public void testExchangeCodeRejectsIncompleteCode() {
        final var code = codeRecord();
        code.setUserId(null);
        when(oidcProviderDao.redeemAuthorizationCode("the-code")).thenReturn(Optional.of(code));
        assertOAuth2Error(ERROR_INVALID_GRANT, () -> getService().exchangeCode(exchangeRequest()));
    }

    @Test
    public void testExchangeCodeRejectsBadClientSecret() {
        when(oidcProviderDao.verifyClientSecret(CLIENT_ID, CLIENT_SECRET)).thenReturn(false);
        assertOAuth2Error(ERROR_INVALID_CLIENT, () -> getService().exchangeCode(exchangeRequest()));
    }

    @Test
    public void testExchangeCodeRejectsGrantTypeNotAllowed() {
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.REFRESH_TOKEN));
        when(oidcProviderDao.redeemAuthorizationCode("the-code")).thenReturn(Optional.of(codeRecord()));
        assertOAuth2Error(ERROR_UNAUTHORIZED_CLIENT, () -> getService().exchangeCode(exchangeRequest()));
    }

    @Test
    public void testRefreshRotatesSessionAndTokens() {

        final var grant = new OidcProviderGrant();
        grant.setId("grant-1");
        grant.setClientId(CLIENT_ID);
        grant.setUserId("user-1");
        grant.setScope("openid profile");
        grant.setSessionId("sid-1");
        grant.setExpiry(new Timestamp(currentTimeMillis() + 2_000_000));

        final var consumed = new OidcRefreshToken();
        consumed.setId("rt-1");
        consumed.setSecretHash("hash");
        consumed.setGrantId("grant-1");
        consumed.setClientId(CLIENT_ID);
        consumed.setStatus(OidcRefreshToken.Status.CONSUMED);

        when(oidcProviderDao.consumeRefreshToken(anyString())).thenReturn(Optional.of(consumed));
        when(oidcProviderDao.getGrant("grant-1")).thenReturn(Optional.of(grant));
        when(oidcProviderDao.rotateGrantSession("grant-1", "sid-1", "sid-1")).thenReturn(Optional.of(grant));
        when(oidcProviderDao.createRefreshToken(any())).then(i -> i.getArgument(0));

        final var request = new OidcProviderTokenRequest();
        request.setGrantType("refresh_token");
        request.setRefreshToken("presented-raw-token");
        request.setClientId(CLIENT_ID);
        request.setClientSecret(CLIENT_SECRET);

        final var response = getService().refresh(request);

        assertEquals(response.getAccessToken(), "access-token-value");
        assertNotNull(response.getRefreshToken());
        assertEquals(response.getScope(), "openid profile");

        // The grant was rotated off the old session, and the old session was revoked.
        verify(oidcProviderDao).rotateGrantSession("grant-1", "sid-1", "sid-1");
        verify(sessionDao).deleteSessionBySessionId("sid-1");

    }

    @Test
    public void testRefreshLosesRotationRaceDeletesNewSession() {

        final var grant = new OidcProviderGrant();
        grant.setId("grant-1");
        grant.setClientId(CLIENT_ID);
        grant.setUserId("user-1");
        grant.setSessionId("sid-1");
        grant.setExpiry(new Timestamp(currentTimeMillis() + 2_000_000));

        final var consumed = new OidcRefreshToken();
        consumed.setGrantId("grant-1");
        consumed.setClientId(CLIENT_ID);

        when(oidcProviderDao.consumeRefreshToken(anyString())).thenReturn(Optional.of(consumed));
        when(oidcProviderDao.getGrant("grant-1")).thenReturn(Optional.of(grant));
        when(oidcProviderDao.rotateGrantSession("grant-1", "sid-1", "sid-1")).thenReturn(Optional.empty());

        final var request = new OidcProviderTokenRequest();
        request.setGrantType("refresh_token");
        request.setRefreshToken("presented-raw-token");
        request.setClientId(CLIENT_ID);
        request.setClientSecret(CLIENT_SECRET);

        assertOAuth2Error(ERROR_INVALID_GRANT, () -> getService().refresh(request));

        // The freshly-issued session must not remain live when the rotation is lost.
        verify(sessionDao).deleteSessionBySessionId("sid-1");

    }

    @Test
    public void testRefreshRejectsReplayedToken() {
        when(oidcProviderDao.consumeRefreshToken(anyString())).thenReturn(Optional.empty());
        final var request = new OidcProviderTokenRequest();
        request.setGrantType("refresh_token");
        request.setRefreshToken("presented-raw-token");
        request.setClientId(CLIENT_ID);
        request.setClientSecret(CLIENT_SECRET);
        assertOAuth2Error(ERROR_INVALID_GRANT, () -> getService().refresh(request));
    }

    @Test
    public void testClientCredentialsIssuesAudienceScopedMachineToken() throws Exception {

        registration.setAudiences(Set.of("filebrowser", "llm"));
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.CLIENT_CREDENTIALS));

        final var response = getService().clientCredentials(machineRequest("filebrowser", "openid profile"));

        // A machine token is a standalone RS256 JWT verifiable against the published JWKS, not an Elements
        // session token: there is no end-user, so no session is minted and none is revocable.
        final var accessToken = SignedJWT.parse(response.getAccessToken());
        assertTrue(accessToken.verify(new com.nimbusds.jose.crypto.RSASSAVerifier(publicKey)));
        assertEquals(accessToken.getHeader().getKeyID(), "kid-1");
        assertEquals(accessToken.getJWTClaimsSet().getIssuer(), "https://elements.example.com/api/rest");
        assertEquals(accessToken.getJWTClaimsSet().getSubject(), CLIENT_ID);
        assertEquals(accessToken.getJWTClaimsSet().getAudience().get(0), "filebrowser");
        assertEquals(accessToken.getJWTClaimsSet().getStringClaim("azp"), CLIENT_ID);
        assertEquals(accessToken.getJWTClaimsSet().getStringClaim("scope"), "openid profile");
        assertNotNull(accessToken.getJWTClaimsSet().getJWTID());

        assertEquals(response.getTokenType(), "Bearer");
        assertEquals(response.getExpiresIn(), 300L);
        assertEquals(response.getScope(), "openid profile");

        // No end-user means no id_token, no refresh token, and no grant/backing session.
        assertNull(response.getIdToken());
        assertNull(response.getRefreshToken());
        verify(sessionTokenIssuer, never()).issue(any());

    }

    @Test
    public void testClientCredentialsDefaultsToSoleRegisteredAudience() throws Exception {

        registration.setAudiences(Set.of("filebrowser"));
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.CLIENT_CREDENTIALS));

        final var response = getService().clientCredentials(machineRequest(null, null));

        final var accessToken = SignedJWT.parse(response.getAccessToken());
        assertEquals(accessToken.getJWTClaimsSet().getAudience().get(0), "filebrowser");
        assertEquals(response.getScope(), "email openid profile");

    }

    @Test
    public void testClientCredentialsRequiresAudienceWhenSeveralRegistered() {
        registration.setAudiences(Set.of("filebrowser", "llm"));
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.CLIENT_CREDENTIALS));
        assertOAuth2Error(ERROR_INVALID_TARGET, () -> getService().clientCredentials(machineRequest(null, null)));
    }

    @Test
    public void testClientCredentialsRejectsUnregisteredAudience() {
        registration.setAudiences(Set.of("filebrowser"));
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.CLIENT_CREDENTIALS));
        assertOAuth2Error(ERROR_INVALID_TARGET, () -> getService().clientCredentials(machineRequest("other", null)));
    }

    @Test
    public void testClientCredentialsRejectsAudienceWhenNoneRegistered() {
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.CLIENT_CREDENTIALS));
        assertOAuth2Error(ERROR_INVALID_TARGET, () -> getService().clientCredentials(machineRequest("other", null)));
    }

    @Test
    public void testClientCredentialsRejectsScopeBeyondRegistration() {
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.CLIENT_CREDENTIALS));
        assertOAuth2Error(ERROR_INVALID_SCOPE, () -> getService().clientCredentials(machineRequest(null, "openid admin")));
    }

    @Test
    public void testClientCredentialsRejectsGrantTypeNotAllowed() {
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.AUTHORIZATION_CODE));
        assertOAuth2Error(ERROR_UNAUTHORIZED_CLIENT, () -> getService().clientCredentials(machineRequest(null, null)));
    }

    @Test
    public void testClientCredentialsRejectsBadClientSecret() {
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.CLIENT_CREDENTIALS));
        when(oidcProviderDao.verifyClientSecret(CLIENT_ID, CLIENT_SECRET)).thenReturn(false);
        assertOAuth2Error(ERROR_INVALID_CLIENT, () -> getService().clientCredentials(machineRequest(null, null)));
    }

    private OidcProviderTokenRequest machineRequest(final String audience, final String scope) {
        final var request = new OidcProviderTokenRequest();
        request.setGrantType("client_credentials");
        request.setAudience(audience);
        request.setScope(scope);
        request.setClientId(CLIENT_ID);
        request.setClientSecret(CLIENT_SECRET);
        return request;
    }

    /**
     * Asserts the callable fails with an {@link OAuth2Exception} carrying the supplied RFC 6749 error code.
     *
     * @param expectedError the expected OAuth 2.0 error code
     * @param callable the call expected to fail
     * @return the thrown exception, for further assertions
     */
    private OAuth2Exception assertOAuth2Error(final String expectedError, final Supplier<?> callable) {

        try {
            callable.get();
        } catch (final OAuth2Exception ex) {
            assertEquals(ex.getError(), expectedError);
            return ex;
        }

        throw new AssertionError("Expected OAuth2Exception with error " + expectedError + ", but none was thrown.");

    }

    public StandardOidcProviderTokenService getService() {
        return service;
    }

}
