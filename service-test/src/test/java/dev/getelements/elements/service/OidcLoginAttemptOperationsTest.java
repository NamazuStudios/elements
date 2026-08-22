package dev.getelements.elements.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.OidcLoginAttemptDao;
import dev.getelements.elements.sdk.dao.OidcProviderConfigurationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttempt;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttemptStatus;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.sdk.model.auth.TokenEndpointAuthMethod;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptStatusResponse;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.service.auth.oidc.AnonOidcAuthService;
import dev.getelements.elements.service.auth.oidc.OidcAuthServiceOperations;
import dev.getelements.elements.service.auth.oidc.OidcDiscoveryDocument;
import dev.getelements.elements.service.auth.oidc.OidcLinkClaims;
import dev.getelements.elements.service.auth.oidc.OidcLoginAttemptOperations;
import dev.getelements.elements.service.auth.oidc.OidcProviderConfigurationOperations;
import dev.getelements.elements.service.auth.oidc.UserOidcAuthService;
import dev.getelements.elements.sdk.ElementRegistry;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.System.currentTimeMillis;
import static java.util.List.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class OidcLoginAttemptOperationsTest {

    private static final String PROVIDER = "twitch";
    private static final String AUTHORIZATION_ENDPOINT = "https://id.twitch.tv/oauth2/authorize";
    private static final String TOKEN_ENDPOINT = "https://id.twitch.tv/oauth2/token";
    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String REDIRECT_URI = "https://api.example.com/oidc/twitch/callback";
    private static final String ISSUER = "https://id.twitch.tv/oauth2";
    private static final String KID = "test-kid";

    private OidcProviderConfigurationDao oidcProviderConfigurationDao;
    private OidcLoginAttemptDao oidcLoginAttemptDao;
    private OidcProviderConfigurationOperations oidcProviderConfigurationOperations;
    private OidcAuthServiceOperations oidcAuthServiceOperations;
    private AnonOidcAuthService anonOidcAuthService;
    private UserOidcAuthService userOidcAuthService;
    private UserDao userDao;
    private Client client;
    private ApplicationDao applicationDao;
    private ProfileDao profileDao;

    private OidcLoginAttemptOperations operations;

    private RSAPublicKey publicKey;
    private Algorithm algorithm;

    @BeforeMethod
    public void setup() throws Exception {

        final var kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        final var kp = kpg.generateKeyPair();
        publicKey = (RSAPublicKey) kp.getPublic();
        algorithm = Algorithm.RSA256(publicKey, (RSAPrivateKey) kp.getPrivate());

        oidcProviderConfigurationDao = mock(OidcProviderConfigurationDao.class);
        oidcLoginAttemptDao = mock(OidcLoginAttemptDao.class);
        oidcProviderConfigurationOperations = mock(OidcProviderConfigurationOperations.class);
        anonOidcAuthService = mock(AnonOidcAuthService.class);
        client = mock(Client.class);

        // Real UserOidcAuthService — exercises the actual uid-linking logic rather than mocking it away.
        userOidcAuthService = new UserOidcAuthService();
        final var userUidDao = mock(UserUidDao.class);
        when(userUidDao.createUserUidStrict(any(UserUid.class))).then(i -> i.getArgument(0));
        userOidcAuthService.setUserUidDao(userUidDao);
        userOidcAuthService.setUserDao(mock(UserDao.class));
        userOidcAuthService.setElementRegistry(mock(ElementRegistry.class));

        userDao = mock(UserDao.class);

        // Real OidcAuthServiceOperations — exercises the actual shared validation/session-building code rather
        // than mocking it away. The token carries an 'aud' claim (needed for the audience check), which also
        // triggers buildSession's optional Application lookup, so ApplicationDao/SessionDao need mocking too.
        oidcAuthServiceOperations = new OidcAuthServiceOperations();
        applicationDao = mock(ApplicationDao.class);
        oidcAuthServiceOperations.setApplicationDao(applicationDao);
        profileDao = mock(ProfileDao.class);
        oidcAuthServiceOperations.setProfileDao(profileDao);
        oidcAuthServiceOperations.setNameService(mock(NameService.class));
        final var sessionDao = mock(SessionDao.class);
        when(sessionDao.create(any())).thenAnswer(invocation -> {
            final var sessionCreation = new SessionCreation();
            sessionCreation.setSessionSecret("secret");
            sessionCreation.setSession(invocation.getArgument(0));
            return sessionCreation;
        });
        oidcAuthServiceOperations.setSessionDao(sessionDao);
        oidcAuthServiceOperations.setSessionTimeoutSeconds(300L);

        operations = new OidcLoginAttemptOperations();
        operations.setOidcProviderConfigurationDao(oidcProviderConfigurationDao);
        operations.setOidcLoginAttemptDao(oidcLoginAttemptDao);
        operations.setOidcProviderConfigurationOperations(oidcProviderConfigurationOperations);
        operations.setOidcAuthServiceOperations(oidcAuthServiceOperations);
        operations.setAnonOidcAuthService(anonOidcAuthService);
        operations.setUserOidcAuthService(userOidcAuthService);
        operations.setUserDao(userDao);
        operations.setClient(client);
        operations.setTtlSeconds(300L);

        when(oidcProviderConfigurationDao.findByName(PROVIDER)).thenReturn(Optional.of(config()));
        when(oidcProviderConfigurationOperations.resolveDiscovery(any())).thenReturn(discoveryDocument());
        when(oidcProviderConfigurationOperations.resolveScheme(any(), any())).thenReturn(scheme());

    }

    private OidcProviderConfiguration config() {
        final var config = new OidcProviderConfiguration();
        config.setName(PROVIDER);
        config.setDiscoveryUrl("https://id.twitch.tv/oauth2/.well-known/openid-configuration");
        config.setClientId(CLIENT_ID);
        config.setClientSecret(CLIENT_SECRET);
        config.setScopes(of("openid", "user:read:email"));
        config.setRedirectUri(REDIRECT_URI);
        config.setExtraAuthorizeParams(Map.of("claims", "email"));
        config.setTokenEndpointAuthMethod(TokenEndpointAuthMethod.CLIENT_SECRET_POST);
        return config;
    }

    private OidcDiscoveryDocument discoveryDocument() {
        final var doc = new OidcDiscoveryDocument();
        doc.setIssuer(ISSUER);
        doc.setAuthorizationEndpoint(AUTHORIZATION_ENDPOINT);
        doc.setTokenEndpoint(TOKEN_ENDPOINT);
        return doc;
    }

    private OidcAuthScheme scheme() {
        final var n = Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray());
        final var e = Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray());
        final var jwk = new JWK("RS256", KID, "RSA", "sig", e, n);

        final var scheme = new OidcAuthScheme();
        scheme.setName(PROVIDER);
        scheme.setIssuer(ISSUER);
        scheme.setKeys(of(jwk));
        return scheme;
    }

    private OidcLoginAttempt pendingAttempt(final String state, final String nonce) {
        final var attempt = new OidcLoginAttempt();
        attempt.setId("test-id");
        attempt.setProvider(PROVIDER);
        attempt.setState(state);
        attempt.setNonce(nonce);
        attempt.setStatus(OidcLoginAttemptStatus.PENDING);
        attempt.setExpiry(new Timestamp(currentTimeMillis() + 300_000));
        return attempt;
    }

    // ── begin() ──────────────────────────────────────────────────────────────

    @Test(expectedExceptions = InvalidDataException.class)
    public void testBeginWithUnknownProviderThrows() {
        when(oidcProviderConfigurationDao.findByName("unknown")).thenReturn(Optional.empty());
        operations.begin("unknown");
    }

    @Test
    public void testBeginBuildsAuthorizeUrlWithAllRequiredParamsAndNeverTheSecret() {

        final var begin = operations.begin(PROVIDER);

        assertNotNull(begin.getId());
        assertTrue(begin.getExpiresAt() > currentTimeMillis() / 1000);
        assertNotNull(begin.getConfirmToken());
        assertFalse(begin.getConfirmToken().isBlank());

        final var url = begin.getAuthorizeUrl();
        assertTrue(url.startsWith(AUTHORIZATION_ENDPOINT));
        assertTrue(url.contains("response_type=code"));
        assertTrue(url.contains("client_id=" + CLIENT_ID));
        assertTrue(url.contains("state="));
        assertTrue(url.contains("nonce="));
        assertTrue(url.contains("scope=openid"));
        assertTrue(url.contains("claims=email"));
        assertFalse(url.contains(CLIENT_SECRET), "authorize URL must never contain the client secret");
        assertFalse(url.contains(begin.getConfirmToken()),
                "confirmToken must never travel through the browser/IdP leg of the flow");

        final var attemptCaptor = ArgumentCaptor.forClass(OidcLoginAttempt.class);
        verify(oidcLoginAttemptDao).create(attemptCaptor.capture());

        final var persisted = attemptCaptor.getValue();
        assertEquals(persisted.getId(), begin.getId());
        assertEquals(persisted.getProvider(), PROVIDER);
        assertEquals(persisted.getStatus(), OidcLoginAttemptStatus.PENDING);
        assertNotNull(persisted.getState());
        assertNotNull(persisted.getNonce());
        assertEquals(persisted.getConfirmToken(), begin.getConfirmToken());

    }

    @Test
    public void testBeginPersistsConfiguredRedirectUrls() {

        final var config = config();
        config.setSuccessRedirectUrl("https://game.example.com/success");
        config.setErrorRedirectUrl("https://game.example.com/error");
        when(oidcProviderConfigurationDao.findByName(PROVIDER)).thenReturn(Optional.of(config));

        operations.begin(PROVIDER);

        final var attemptCaptor = ArgumentCaptor.forClass(OidcLoginAttempt.class);
        verify(oidcLoginAttemptDao).create(attemptCaptor.capture());

        final var persisted = attemptCaptor.getValue();
        assertEquals(persisted.getSuccessRedirectUrl(), "https://game.example.com/success");
        assertEquals(persisted.getErrorRedirectUrl(), "https://game.example.com/error");

    }

    @Test
    public void testBeginWithoutLinkingUserLeavesLinkedUserIdNull() {

        operations.begin(PROVIDER);

        final var attemptCaptor = ArgumentCaptor.forClass(OidcLoginAttempt.class);
        verify(oidcLoginAttemptDao).create(attemptCaptor.capture());
        assertNull(attemptCaptor.getValue().getLinkedUserId());

    }

    @Test
    public void testBeginWithLinkingUserStampsLinkedUserId() {

        final var user = new User();
        user.setId("current-user-id");

        operations.begin(PROVIDER, user);

        final var attemptCaptor = ArgumentCaptor.forClass(OidcLoginAttempt.class);
        verify(oidcLoginAttemptDao).create(attemptCaptor.capture());
        assertEquals(attemptCaptor.getValue().getLinkedUserId(), "current-user-id");

    }

    @Test
    public void testBeginWithoutApplicationNameOrIdLeavesItNull() {

        operations.begin(PROVIDER);

        final var attemptCaptor = ArgumentCaptor.forClass(OidcLoginAttempt.class);
        verify(oidcLoginAttemptDao).create(attemptCaptor.capture());
        assertNull(attemptCaptor.getValue().getApplicationNameOrId());

    }

    @Test
    public void testBeginWithApplicationNameOrIdPersistsIt() {

        operations.begin(PROVIDER, null, "app-1");

        final var attemptCaptor = ArgumentCaptor.forClass(OidcLoginAttempt.class);
        verify(oidcLoginAttemptDao).create(attemptCaptor.capture());
        assertEquals(attemptCaptor.getValue().getApplicationNameOrId(), "app-1");

    }

    // ── handleCallback() ─────────────────────────────────────────────────────

    @Test
    public void testHandleCallbackWithProviderErrorFailsClosedWithoutExchange() {

        when(oidcLoginAttemptDao.findPendingByState(PROVIDER, "some-state")).thenReturn(Optional.empty());

        final var result = operations.handleCallback(PROVIDER, null, "some-state", "access_denied");

        assertFalse(result.isSuccess());
        assertNull(result.getRedirectUrl());
        verify(oidcLoginAttemptDao).markFailed(eq("some-state"), anyString());
        verifyNoInteractions(client);

    }

    @Test
    public void testHandleCallbackWithProviderErrorUsesConfiguredErrorRedirectUrl() {

        final var attempt = pendingAttempt("some-state", "some-nonce");
        attempt.setErrorRedirectUrl("https://game.example.com/error");
        when(oidcLoginAttemptDao.findPendingByState(PROVIDER, "some-state")).thenReturn(Optional.of(attempt));

        final var result = operations.handleCallback(PROVIDER, null, "some-state", "access_denied");

        assertFalse(result.isSuccess());
        assertEquals(result.getRedirectUrl(), "https://game.example.com/error");

    }

    @Test
    public void testHandleCallbackWithUnknownStateFailsClosed() {
        when(oidcLoginAttemptDao.findPendingByState(PROVIDER, "unknown-state")).thenReturn(Optional.empty());
        final var result = operations.handleCallback(PROVIDER, "some-code", "unknown-state", null);
        assertFalse(result.isSuccess());
        assertNull(result.getRedirectUrl());
    }

    @Test
    public void testHandleCallbackHappyPathValidatesWithAttemptNonceAndClientIdThenMarksComplete() {

        final var state = "matching-state";
        final var nonce = "matching-nonce";
        final var attempt = pendingAttempt(state, nonce);

        when(oidcLoginAttemptDao.findPendingByState(PROVIDER, state)).thenReturn(Optional.of(attempt));
        when(oidcLoginAttemptDao.markComplete(eq(state), anyString())).thenReturn(Optional.of(attempt));

        final var idToken = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("twitch-sub")
                .withAudience(CLIENT_ID)
                .withClaim("nonce", nonce)
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000))
                .sign(algorithm);

        stubTokenExchange(idToken);

        final var sessionCreation = new SessionCreation();
        sessionCreation.setSessionSecret("secret");
        when(anonOidcAuthService.apply(any(), any())).thenReturn(null);

        final var result = operations.handleCallback(PROVIDER, "auth-code", state, null);

        assertTrue(result.isSuccess());
        assertNull(result.getRedirectUrl());
        verify(oidcLoginAttemptDao).markComplete(eq(state), anyString());
        verify(oidcLoginAttemptDao, never()).markFailed(eq(state), anyString());

    }

    @Test
    public void testHandleCallbackAnonymousPathReachesGatedAutoCreateWhenApplicationNameOrIdSet() {

        final var state = "matching-state";
        final var nonce = "matching-nonce";
        final var attempt = pendingAttempt(state, nonce);
        attempt.setApplicationNameOrId("app-1");

        when(oidcLoginAttemptDao.findPendingByState(PROVIDER, state)).thenReturn(Optional.of(attempt));
        when(oidcLoginAttemptDao.markComplete(eq(state), anyString())).thenReturn(Optional.of(attempt));

        final var idToken = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("twitch-sub")
                .withAudience(CLIENT_ID)
                .withClaim("nonce", nonce)
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000))
                .sign(algorithm);

        stubTokenExchange(idToken);

        final var user = new User();
        user.setId("user-1");
        when(anonOidcAuthService.apply(any(), any())).thenReturn(user);

        final var application = new Application();
        application.setId("app-1");
        application.setAutoCreateProfile(true);
        application.setMaxProfiles(1);

        when(applicationDao.findActiveApplication("app-1")).thenReturn(Optional.of(application));
        when(profileDao.findPrimaryProfile("user-1", "app-1")).thenReturn(Optional.empty());

        final var createdProfile = new Profile();
        createdProfile.setId("profile-1");
        when(profileDao.createSlottedProfile(any(Profile.class), anyMap())).thenReturn(createdProfile);

        final var result = operations.handleCallback(PROVIDER, "auth-code", state, null);

        assertTrue(result.isSuccess());
        verify(profileDao).createSlottedProfile(any(Profile.class), anyMap());
        verify(profileDao, never()).createOrRefreshProfile(any());

    }

    @Test
    public void testHandleCallbackAnonymousPathWithoutApplicationNameOrIdKeepsLegacyUngatedBehavior() {

        final var state = "matching-state";
        final var nonce = "matching-nonce";
        final var attempt = pendingAttempt(state, nonce);
        // applicationNameOrId intentionally left unset -- legacy aud-claim-driven resolution applies.

        when(oidcLoginAttemptDao.findPendingByState(PROVIDER, state)).thenReturn(Optional.of(attempt));
        when(oidcLoginAttemptDao.markComplete(eq(state), anyString())).thenReturn(Optional.of(attempt));

        final var idToken = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("twitch-sub")
                .withAudience(CLIENT_ID)
                .withClaim("nonce", nonce)
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000))
                .sign(algorithm);

        stubTokenExchange(idToken);

        final var user = new User();
        user.setId("user-2");
        when(anonOidcAuthService.apply(any(), any())).thenReturn(user);

        final var application = new Application();
        application.setId(CLIENT_ID);
        application.setAutoCreateProfile(true);
        application.setMaxProfiles(1);

        when(applicationDao.findActiveApplication(CLIENT_ID)).thenReturn(Optional.of(application));
        when(profileDao.findPrimaryProfile("user-2", CLIENT_ID)).thenReturn(Optional.empty());

        final var legacyProfile = new Profile();
        legacyProfile.setId("legacy-profile");
        when(profileDao.createOrRefreshProfile(any(Profile.class))).thenReturn(legacyProfile);

        final var result = operations.handleCallback(PROVIDER, "auth-code", state, null);

        assertTrue(result.isSuccess());
        verify(profileDao).createOrRefreshProfile(any(Profile.class));
        verify(profileDao, never()).createSlottedProfile(any(), anyMap());

    }

    @Test
    public void testHandleCallbackHappyPathUsesConfiguredSuccessRedirectUrl() {

        final var state = "matching-state";
        final var nonce = "matching-nonce";
        final var attempt = pendingAttempt(state, nonce);
        attempt.setSuccessRedirectUrl("https://game.example.com/success");

        when(oidcLoginAttemptDao.findPendingByState(PROVIDER, state)).thenReturn(Optional.of(attempt));
        when(oidcLoginAttemptDao.markComplete(eq(state), anyString())).thenReturn(Optional.of(attempt));

        final var idToken = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("twitch-sub")
                .withAudience(CLIENT_ID)
                .withClaim("nonce", nonce)
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000))
                .sign(algorithm);

        stubTokenExchange(idToken);
        when(anonOidcAuthService.apply(any(), any())).thenReturn(null);

        final var result = operations.handleCallback(PROVIDER, "auth-code", state, null);

        assertTrue(result.isSuccess());
        assertEquals(result.getRedirectUrl(), "https://game.example.com/success");

    }

    @Test
    public void testHandleCallbackForLinkingAttemptMarksLinkReadyWithoutMutating() {

        final var state = "linking-state";
        final var nonce = "linking-nonce";
        final var attempt = pendingAttempt(state, nonce);
        attempt.setLinkedUserId("current-user-id");

        when(oidcLoginAttemptDao.findPendingByState(PROVIDER, state)).thenReturn(Optional.of(attempt));
        when(oidcLoginAttemptDao.markLinkReady(eq(state), anyString())).thenReturn(Optional.of(attempt));

        final var idToken = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("twitch-sub")
                .withAudience(CLIENT_ID)
                .withClaim("nonce", nonce)
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000))
                .sign(algorithm);

        stubTokenExchange(idToken);

        final var result = operations.handleCallback(PROVIDER, "auth-code", state, null);

        assertTrue(result.isSuccess());
        verify(oidcLoginAttemptDao).markLinkReady(eq(state), anyString());
        verify(oidcLoginAttemptDao, never()).markComplete(any(), any());
        verifyNoInteractions(anonOidcAuthService);
        verifyNoInteractions(userDao);

        final var claimsCaptor = ArgumentCaptor.forClass(String.class);
        verify(oidcLoginAttemptDao).markLinkReady(eq(state), claimsCaptor.capture());
        assertTrue(claimsCaptor.getValue().contains("twitch-sub"));

    }

    @Test
    public void testHandleCallbackForLinkingAttemptWithApplicationNameOrIdPersistsItExplicitlyOntoClaims() {

        final var state = "linking-state";
        final var nonce = "linking-nonce";
        final var attempt = pendingAttempt(state, nonce);
        attempt.setLinkedUserId("current-user-id");
        attempt.setApplicationNameOrId("app-1");

        when(oidcLoginAttemptDao.findPendingByState(PROVIDER, state)).thenReturn(Optional.of(attempt));
        when(oidcLoginAttemptDao.markLinkReady(eq(state), anyString())).thenReturn(Optional.of(attempt));

        final var idToken = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("twitch-sub")
                .withAudience(CLIENT_ID)
                .withClaim("nonce", nonce)
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000))
                .sign(algorithm);

        stubTokenExchange(idToken);

        operations.handleCallback(PROVIDER, "auth-code", state, null);

        final var claims = deserializeLinkClaims();
        assertEquals(claims.getApplicationNameOrId(), "app-1");
        assertTrue(claims.isApplicationExplicitlyRequested());

    }

    @Test
    public void testHandleCallbackForLinkingAttemptWithoutApplicationNameOrIdFallsBackToAudClaim() {

        final var state = "linking-state";
        final var nonce = "linking-nonce";
        final var attempt = pendingAttempt(state, nonce);
        attempt.setLinkedUserId("current-user-id");
        // applicationNameOrId intentionally left unset -- aud-claim fallback applies, same as the anonymous path.

        when(oidcLoginAttemptDao.findPendingByState(PROVIDER, state)).thenReturn(Optional.of(attempt));
        when(oidcLoginAttemptDao.markLinkReady(eq(state), anyString())).thenReturn(Optional.of(attempt));

        final var idToken = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("twitch-sub")
                .withAudience(CLIENT_ID)
                .withClaim("nonce", nonce)
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000))
                .sign(algorithm);

        stubTokenExchange(idToken);

        operations.handleCallback(PROVIDER, "auth-code", state, null);

        final var claims = deserializeLinkClaims();
        assertEquals(claims.getApplicationNameOrId(), CLIENT_ID);
        assertFalse(claims.isApplicationExplicitlyRequested());

    }

    private OidcLinkClaims deserializeLinkClaims() {
        final var claimsCaptor = ArgumentCaptor.forClass(String.class);
        verify(oidcLoginAttemptDao).markLinkReady(anyString(), claimsCaptor.capture());
        try {
            return new ObjectMapper().readValue(claimsCaptor.getValue(), OidcLinkClaims.class);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // ── poll() ───────────────────────────────────────────────────────────────

    @Test(expectedExceptions = ForbiddenException.class)
    public void testPollOnLinkReadyAttemptThrows() {
        when(oidcLoginAttemptDao.claimCompleteById("link-ready-id")).thenReturn(Optional.empty());
        when(oidcLoginAttemptDao.findLinkReadyById("link-ready-id"))
                .thenReturn(Optional.of(linkReadyAttempt()));
        operations.poll("link-ready-id");
    }

    // ── confirmLink() ────────────────────────────────────────────────────────

    @Test
    public void testConfirmLinkWithCorrectTokenPerformsLinkAndReturnsSession() {

        final var attempt = linkReadyAttempt();

        when(oidcLoginAttemptDao.findLinkReadyById("link-ready-id")).thenReturn(Optional.of(attempt));
        when(oidcLoginAttemptDao.claimLinkReadyById("link-ready-id")).thenReturn(Optional.of(attempt));

        final var linkedUser = new User();
        linkedUser.setId("current-user-id");
        when(userDao.getUser("current-user-id")).thenReturn(linkedUser);

        final var result = operations.confirmLink("link-ready-id", "correct-token");

        assertEquals(result.getStatus(), dev.getelements.elements.sdk.model.session.OidcLoginAttemptState.COMPLETE);
        assertNotNull(result.getSession());
        verify(userDao).getUser("current-user-id");
        verify(oidcLoginAttemptDao).claimLinkReadyById("link-ready-id");

    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testConfirmLinkWithWrongTokenThrowsAndDoesNotConsume() {

        when(oidcLoginAttemptDao.findLinkReadyById("link-ready-id"))
                .thenReturn(Optional.of(linkReadyAttempt()));

        try {
            operations.confirmLink("link-ready-id", "wrong-token");
        } finally {
            verify(oidcLoginAttemptDao, never()).claimLinkReadyById(any());
        }

    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testConfirmLinkWithNullTokenThrowsAndDoesNotConsume() {
        when(oidcLoginAttemptDao.findLinkReadyById("link-ready-id"))
                .thenReturn(Optional.of(linkReadyAttempt()));
        try {
            operations.confirmLink("link-ready-id", null);
        } finally {
            verify(oidcLoginAttemptDao, never()).claimLinkReadyById(any());
        }
    }

    @Test
    public void testConfirmLinkOnUnknownIdReturnsExpired() {
        when(oidcLoginAttemptDao.findLinkReadyById("unknown-id")).thenReturn(Optional.empty());
        final var result = operations.confirmLink("unknown-id", "any-token");
        assertEquals(result.getStatus(), dev.getelements.elements.sdk.model.session.OidcLoginAttemptState.EXPIRED);
    }

    @Test
    public void testConfirmLinkLosingClaimRaceReturnsExpired() {

        when(oidcLoginAttemptDao.findLinkReadyById("link-ready-id"))
                .thenReturn(Optional.of(linkReadyAttempt()));
        when(oidcLoginAttemptDao.claimLinkReadyById("link-ready-id")).thenReturn(Optional.empty());

        final var result = operations.confirmLink("link-ready-id", "correct-token");

        assertEquals(result.getStatus(), dev.getelements.elements.sdk.model.session.OidcLoginAttemptState.EXPIRED);
        verifyNoInteractions(userDao);

    }

    @Test
    public void testConfirmLinkReusesExistingPrimaryProfileIfPresent() {

        final var attempt = linkReadyAttemptWithApplication("app-1", true);
        when(oidcLoginAttemptDao.findLinkReadyById("link-ready-id")).thenReturn(Optional.of(attempt));
        when(oidcLoginAttemptDao.claimLinkReadyById("link-ready-id")).thenReturn(Optional.of(attempt));

        final var linkedUser = new User();
        linkedUser.setId("current-user-id");
        when(userDao.getUser("current-user-id")).thenReturn(linkedUser);

        final var application = new Application();
        application.setId("app-1");
        application.setAutoCreateProfile(true);
        application.setMaxProfiles(1);

        final var existingProfile = new Profile();
        existingProfile.setId("existing-profile");

        when(applicationDao.findActiveApplication("app-1")).thenReturn(Optional.of(application));
        when(profileDao.findPrimaryProfile("current-user-id", "app-1")).thenReturn(Optional.of(existingProfile));

        final var result = operations.confirmLink("link-ready-id", "correct-token");

        assertEquals(result.getStatus(), dev.getelements.elements.sdk.model.session.OidcLoginAttemptState.COMPLETE);
        verify(profileDao, never()).createSlottedProfile(any(), anyMap());
        verify(profileDao, never()).createOrRefreshProfile(any());
        assertEquals(result.getSession().getSession().getProfile(), existingProfile);
        assertEquals(result.getSession().getSession().getApplication(), application);

    }

    @Test
    public void testConfirmLinkAutoCreatesGatedProfileWhenExplicitlyRequestedAndNoneExists() {

        final var attempt = linkReadyAttemptWithApplication("app-2", true);
        when(oidcLoginAttemptDao.findLinkReadyById("link-ready-id")).thenReturn(Optional.of(attempt));
        when(oidcLoginAttemptDao.claimLinkReadyById("link-ready-id")).thenReturn(Optional.of(attempt));

        final var linkedUser = new User();
        linkedUser.setId("current-user-id");
        when(userDao.getUser("current-user-id")).thenReturn(linkedUser);

        final var application = new Application();
        application.setId("app-2");
        application.setAutoCreateProfile(true);
        application.setMaxProfiles(1);

        when(applicationDao.findActiveApplication("app-2")).thenReturn(Optional.of(application));
        when(profileDao.findPrimaryProfile("current-user-id", "app-2")).thenReturn(Optional.empty());

        final var createdProfile = new Profile();
        createdProfile.setId("created-profile");
        when(profileDao.createSlottedProfile(any(Profile.class), anyMap())).thenReturn(createdProfile);

        final var result = operations.confirmLink("link-ready-id", "correct-token");

        assertEquals(result.getStatus(), dev.getelements.elements.sdk.model.session.OidcLoginAttemptState.COMPLETE);
        verify(profileDao).createSlottedProfile(any(Profile.class), anyMap());
        verify(profileDao, never()).createOrRefreshProfile(any());
        assertEquals(result.getSession().getSession().getProfile(), createdProfile);

    }

    @Test
    public void testConfirmLinkUsesLegacyUngatedCreateWhenApplicationCameFromAudClaimFallback() {

        // explicitlyRequested=false mirrors what handleCallback persists when applicationNameOrId was never set
        // on the attempt and the value came from the token's own aud claim instead.
        final var attempt = linkReadyAttemptWithApplication("app-3", false);
        when(oidcLoginAttemptDao.findLinkReadyById("link-ready-id")).thenReturn(Optional.of(attempt));
        when(oidcLoginAttemptDao.claimLinkReadyById("link-ready-id")).thenReturn(Optional.of(attempt));

        final var linkedUser = new User();
        linkedUser.setId("current-user-id");
        when(userDao.getUser("current-user-id")).thenReturn(linkedUser);

        final var application = new Application();
        application.setId("app-3");
        application.setAutoCreateProfile(false); // deliberately not configured -- legacy path ignores this
        application.setMaxProfiles(0);

        when(applicationDao.findActiveApplication("app-3")).thenReturn(Optional.of(application));
        when(profileDao.findPrimaryProfile("current-user-id", "app-3")).thenReturn(Optional.empty());

        final var legacyProfile = new Profile();
        legacyProfile.setId("legacy-profile");
        when(profileDao.createOrRefreshProfile(any(Profile.class))).thenReturn(legacyProfile);

        final var result = operations.confirmLink("link-ready-id", "correct-token");

        assertEquals(result.getStatus(), dev.getelements.elements.sdk.model.session.OidcLoginAttemptState.COMPLETE);
        verify(profileDao).createOrRefreshProfile(any(Profile.class));
        verify(profileDao, never()).createSlottedProfile(any(), anyMap());
        assertEquals(result.getSession().getSession().getProfile(), legacyProfile);

    }

    private OidcLoginAttempt linkReadyAttempt() {
        final var attempt = pendingAttempt("linking-state", "linking-nonce");
        attempt.setId("link-ready-id");
        attempt.setStatus(OidcLoginAttemptStatus.LINK_READY);
        attempt.setLinkedUserId("current-user-id");
        attempt.setConfirmToken("correct-token");
        attempt.setLinkClaimsJson(
                "{\"schemeName\":\"twitch\",\"externalUserId\":\"twitch-sub\",\"email\":null,\"profileClaims\":{}}");
        return attempt;
    }

    private OidcLoginAttempt linkReadyAttemptWithApplication(final String applicationNameOrId,
                                                               final boolean explicitlyRequested) {
        final var attempt = linkReadyAttempt();
        attempt.setLinkClaimsJson(
                "{\"schemeName\":\"twitch\",\"externalUserId\":\"twitch-sub\",\"email\":null,\"profileClaims\":{}," +
                        "\"applicationNameOrId\":\"" + applicationNameOrId + "\"," +
                        "\"applicationExplicitlyRequested\":" + explicitlyRequested + "}");
        return attempt;
    }

    @Test
    public void testHandleCallbackNonceMismatchMarksFailed() {

        final var state = "matching-state";
        final var attempt = pendingAttempt(state, "expected-nonce");

        when(oidcLoginAttemptDao.findPendingByState(PROVIDER, state)).thenReturn(Optional.of(attempt));

        final var idToken = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("twitch-sub")
                .withAudience(CLIENT_ID)
                .withClaim("nonce", "wrong-nonce")
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000))
                .sign(algorithm);

        stubTokenExchange(idToken);

        final var result = operations.handleCallback(PROVIDER, "auth-code", state, null);

        assertFalse(result.isSuccess());
        verify(oidcLoginAttemptDao, never()).markComplete(any(), any());
        verify(oidcLoginAttemptDao).markFailed(eq(state), anyString());

    }

    private void stubTokenExchange(final String idToken) {

        final var target = mock(WebTarget.class);
        final var requestBuilder = mock(Invocation.Builder.class);
        final var response = mock(Response.class);

        when(client.target(TOKEN_ENDPOINT)).thenReturn(target);
        when(target.request(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.post(any(Entity.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(200);

        doAnswer(invocation -> {
            final var tokenResponse = new dev.getelements.elements.service.auth.oidc.OidcTokenResponse();
            tokenResponse.setIdToken(idToken);
            return tokenResponse;
        }).when(response).readEntity(dev.getelements.elements.service.auth.oidc.OidcTokenResponse.class);

    }

}
