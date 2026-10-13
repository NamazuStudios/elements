package dev.getelements.elements.service.auth.oidc.provider;

import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.auth.OAuth2Exception;
import dev.getelements.elements.sdk.model.oidc.OidcProviderAuthorizationRequest;
import dev.getelements.elements.sdk.model.oidc.OidcProviderLoginRequest;
import dev.getelements.elements.sdk.model.user.User;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.CODE_CHALLENGE_METHOD_S256;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_REQUEST;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_SCOPE;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_UNAUTHORIZED_CLIENT;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_UNSUPPORTED_RESPONSE_TYPE;
import static java.lang.System.currentTimeMillis;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class StandardOidcProviderAuthorizationServiceTest {

    private static final String CLIENT_ID = "el_test";
    private static final String REDIRECT_URI = "https://client.example.com/callback";

    private OidcProviderDao oidcProviderDao;

    private UserDao userDao;

    @Inject
    private StandardOidcProviderAuthorizationService service;

    private OidcClientRegistration registration;

    @BeforeMethod
    public void setup() {

        oidcProviderDao = mock(OidcProviderDao.class);
        userDao = mock(UserDao.class);

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(OidcProviderDao.class).toInstance(oidcProviderDao);
                bind(UserDao.class).toInstance(userDao);
                bind(OidcProviderRateLimiter.class);
                bind(boolean.class).annotatedWith(
                        com.google.inject.name.Names.named(
                                dev.getelements.elements.sdk.service.Constants.OIDC_RATE_LIMIT_ENABLED))
                        .toInstance(false);
                bind(long.class).annotatedWith(
                        com.google.inject.name.Names.named(
                                dev.getelements.elements.sdk.service.Constants.OIDC_RATE_LIMIT_TOKEN_PER_MINUTE))
                        .toInstance(60L);
                bind(long.class).annotatedWith(
                        com.google.inject.name.Names.named(
                                dev.getelements.elements.sdk.service.Constants.OIDC_RATE_LIMIT_AUTHORIZE_PER_MINUTE))
                        .toInstance(60L);
                bind(long.class).annotatedWith(
                        com.google.inject.name.Names.named(
                                dev.getelements.elements.sdk.service.Constants.OIDC_RATE_LIMIT_LOGIN_PER_MINUTE))
                        .toInstance(10L);
                bind(long.class).annotatedWith(
                        com.google.inject.name.Names.named(
                                dev.getelements.elements.sdk.service.Constants.OIDC_AUTHORIZATION_CODE_TTL_SECONDS))
                        .toInstance(600L);
                bind(StandardOidcProviderAuthorizationService.class);
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

        final var user = new User();
        user.setId("user-1");
        when(userDao.validateUserPassword(anyString(), anyString())).thenReturn(user);

    }

    private OidcProviderAuthorizationRequest validRequest() {
        final var request = new OidcProviderAuthorizationRequest();
        request.setResponseType("code");
        request.setClientId(CLIENT_ID);
        request.setRedirectUri(REDIRECT_URI);
        request.setScope("openid profile");
        request.setState("state-123");
        request.setNonce("nonce-123");
        request.setCodeChallenge("challenge-value");
        request.setCodeChallengeMethod(CODE_CHALLENGE_METHOD_S256);
        return request;
    }

    @Test
    public void testBeginAuthorizationCreatesPendingCode() {

        when(oidcProviderDao.createAuthorizationCode(any()))
                .then(i -> i.getArgument(0));

        final var code = getService().beginAuthorization(validRequest());

        assertNotNull(code.getCode());
        assertEquals(code.getClientId(), CLIENT_ID);
        assertEquals(code.getState(), "state-123");
        assertEquals(code.getStatus(), OidcAuthorizationCode.Status.PENDING);
        assertTrue(code.getExpiry().after(new java.sql.Timestamp(currentTimeMillis() - 1000)));

        final var captured = org.mockito.ArgumentCaptor.forClass(OidcAuthorizationCode.class);
        verify(oidcProviderDao).createAuthorizationCode(captured.capture());
        assertEquals(captured.getValue().getApplicationId(), null);

    }

    @Test
    public void testBeginAuthorizationRejectsUnknownClient() {
        when(oidcProviderDao.getClient(CLIENT_ID)).thenReturn(Optional.empty());
        assertOAuth2Error(ERROR_INVALID_REQUEST, () -> getService().beginAuthorization(validRequest()));
    }

    @Test
    public void testBeginAuthorizationRejectsSuspendedClient() {
        registration.setStatus(OidcClientRegistration.Status.SUSPENDED);
        assertOAuth2Error(ERROR_UNAUTHORIZED_CLIENT, () -> getService().beginAuthorization(validRequest()));
    }

    @Test
    public void testBeginAuthorizationRejectsUnregisteredRedirectUri() {
        final var request = validRequest();
        request.setRedirectUri("https://evil.example.com/callback");
        assertOAuth2Error(ERROR_INVALID_REQUEST, () -> getService().beginAuthorization(request));
    }

    @Test
    public void testBeginAuthorizationRejectsScopeBeyondRegistration() {

        final var request = validRequest();
        request.setScope("openid profile email admin");

        final var ex = assertOAuth2Error(ERROR_INVALID_SCOPE, () -> getService().beginAuthorization(request));

        // The redirect URI was validated before the scope check, so the error is redirect-able and carries
        // the context the resource needs to send the browser back to the relying party.
        assertEquals(ex.getRedirectUri(), REDIRECT_URI);
        assertEquals(ex.getState(), "state-123");

    }

    @Test
    public void testBeginAuthorizationRejectsMissingOpenidScope() {
        final var request = validRequest();
        request.setScope("profile");
        assertOAuth2Error(ERROR_INVALID_SCOPE, () -> getService().beginAuthorization(request));
    }

    @Test
    public void testBeginAuthorizationRejectsWrongResponseType() {
        final var request = validRequest();
        request.setResponseType("token");
        assertOAuth2Error(ERROR_UNSUPPORTED_RESPONSE_TYPE, () -> getService().beginAuthorization(request));
    }

    @Test
    public void testBeginAuthorizationRejectsWrongChallengeMethod() {
        final var request = validRequest();
        request.setCodeChallengeMethod("plain");
        assertOAuth2Error(ERROR_INVALID_REQUEST, () -> getService().beginAuthorization(request));
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

    @Test
    public void testCompleteAuthorizationAttachesUserAndRedirects() {

        final var pending = validRequest();
        final var code = new OidcAuthorizationCode();
        code.setCode("the-code");
        code.setClientId(CLIENT_ID);
        code.setRedirectUri(REDIRECT_URI);
        code.setState("state-123");
        code.setStatus(OidcAuthorizationCode.Status.PENDING);

        when(oidcProviderDao.createAuthorizationCode(any()))
                .then(i -> i.getArgument(0));

        final var begun = getService().beginAuthorization(validRequest());

        final var completed = new OidcAuthorizationCode();
        completed.setCode(begun.getCode());
        code.setClientId(CLIENT_ID);
        code.setRedirectUri(REDIRECT_URI);
        code.setState("state-123");
        code.setStatus(OidcAuthorizationCode.Status.PENDING);

        when(oidcProviderDao.completeAuthorizationCode(eq(code.getCode()), eq("user-1"), anyLong()))
                .thenReturn(Optional.of(code));

        final var loginRequest = new OidcProviderLoginRequest();
        loginRequest.setAuthorizationId(code.getCode());
        loginRequest.setUsername("user");
        loginRequest.setPassword("password");

        final var redirect = getService().completeAuthorization(loginRequest);

        assertEquals(redirect, REDIRECT_URI + "?code=the-code&state=state-123");

    }

    @Test
    public void testCompleteAuthorizationRejectsUnknownOrConsumedAuthorization() {
        when(oidcProviderDao.completeAuthorizationCode(anyString(), anyString(), anyLong()))
                .thenReturn(Optional.empty());

        final var loginRequest = new OidcProviderLoginRequest();
        loginRequest.setAuthorizationId("bogus");
        loginRequest.setUsername("user");
        loginRequest.setPassword("password");

        assertThrows(InvalidDataException.class, () -> getService().completeAuthorization(loginRequest));
    }

    @Test
    public void testRedirectUriWithExistingQueryUsesAmpersand() {

        final var code = new OidcAuthorizationCode();
        code.setCode("the-code");
        code.setRedirectUri(REDIRECT_URI + "?foo=bar");
        code.setState("state-123");

        when(oidcProviderDao.completeAuthorizationCode(anyString(), anyString(), anyLong()))
                .thenReturn(Optional.of(code));

        final var loginRequest = new OidcProviderLoginRequest();
        loginRequest.setAuthorizationId("auth-id");
        loginRequest.setUsername("user");
        loginRequest.setPassword("password");

        final var redirect = getService().completeAuthorization(loginRequest);

        assertEquals(redirect, REDIRECT_URI + "?foo=bar&code=the-code&state=state-123");

    }

    public StandardOidcProviderAuthorizationService getService() {
        return service;
    }

}
