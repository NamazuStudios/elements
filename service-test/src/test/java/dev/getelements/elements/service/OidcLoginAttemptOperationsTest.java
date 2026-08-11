package dev.getelements.elements.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.OidcLoginAttemptDao;
import dev.getelements.elements.sdk.dao.OidcProviderConfigurationDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttempt;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttemptStatus;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.sdk.model.auth.TokenEndpointAuthMethod;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.service.auth.oidc.AnonOidcAuthService;
import dev.getelements.elements.service.auth.oidc.OidcAuthServiceOperations;
import dev.getelements.elements.service.auth.oidc.OidcDiscoveryDocument;
import dev.getelements.elements.service.auth.oidc.OidcLoginAttemptOperations;
import dev.getelements.elements.service.auth.oidc.OidcProviderConfigurationOperations;
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
    private Client client;

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

        // Real OidcAuthServiceOperations — exercises the actual shared validation/session-building code rather
        // than mocking it away. The token carries an 'aud' claim (needed for the audience check), which also
        // triggers buildSession's optional Application lookup, so ApplicationDao/SessionDao need mocking too.
        oidcAuthServiceOperations = new OidcAuthServiceOperations();
        oidcAuthServiceOperations.setApplicationDao(mock(ApplicationDao.class));
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

        final var url = begin.getAuthorizeUrl();
        assertTrue(url.startsWith(AUTHORIZATION_ENDPOINT));
        assertTrue(url.contains("response_type=code"));
        assertTrue(url.contains("client_id=" + CLIENT_ID));
        assertTrue(url.contains("state="));
        assertTrue(url.contains("nonce="));
        assertTrue(url.contains("scope=openid"));
        assertTrue(url.contains("claims=email"));
        assertFalse(url.contains(CLIENT_SECRET), "authorize URL must never contain the client secret");

        final var attemptCaptor = ArgumentCaptor.forClass(OidcLoginAttempt.class);
        verify(oidcLoginAttemptDao).create(attemptCaptor.capture());

        final var persisted = attemptCaptor.getValue();
        assertEquals(persisted.getId(), begin.getId());
        assertEquals(persisted.getProvider(), PROVIDER);
        assertEquals(persisted.getStatus(), OidcLoginAttemptStatus.PENDING);
        assertNotNull(persisted.getState());
        assertNotNull(persisted.getNonce());

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
