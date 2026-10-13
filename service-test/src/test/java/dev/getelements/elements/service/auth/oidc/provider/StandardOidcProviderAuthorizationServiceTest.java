package dev.getelements.elements.service.auth.oidc.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.auth.OAuth2Exception;
import dev.getelements.elements.sdk.model.oidc.OidcProviderAuthorizationRequest;
import dev.getelements.elements.sdk.model.oidc.OidcProviderConsent;
import dev.getelements.elements.sdk.model.oidc.OidcProviderLoginRequest;
import dev.getelements.elements.sdk.model.user.User;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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

        createInjector(false).injectMembers(this);

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

    private com.google.inject.Injector createInjector(final boolean consentRequired) {
        return Guice.createInjector(new AbstractModule() {
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
                                dev.getelements.elements.sdk.service.Constants.OIDC_RATE_LIMIT_AUTHORIZE_PER_MINUTE))
                        .toInstance(60L);
                bind(long.class).annotatedWith(
                        com.google.inject.name.Names.named(
                                dev.getelements.elements.sdk.service.Constants.OIDC_RATE_LIMIT_TOKEN_PER_MINUTE))
                        .toInstance(60L);
                bind(long.class).annotatedWith(
                        com.google.inject.name.Names.named(
                                dev.getelements.elements.sdk.service.Constants.OIDC_RATE_LIMIT_LOGIN_PER_MINUTE))
                        .toInstance(10L);
                bind(long.class).annotatedWith(
                        com.google.inject.name.Names.named(
                                dev.getelements.elements.sdk.service.Constants.OIDC_AUTHORIZATION_CODE_TTL_SECONDS))
                        .toInstance(600L);
                bind(boolean.class).annotatedWith(
                        com.google.inject.name.Names.named(
                                dev.getelements.elements.sdk.service.Constants.OIDC_PROVIDER_CONSENT_REQUIRED))
                        .toInstance(consentRequired);
                bind(StandardOidcProviderAuthorizationService.class);
            }
        });
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

    private OidcProviderLoginRequest loginRequest(final String authorizationId) {
        final var loginRequest = new OidcProviderLoginRequest();
        loginRequest.setAuthorizationId(authorizationId);
        loginRequest.setUsername("user");
        loginRequest.setPassword("password");
        return loginRequest;
    }

    private OidcAuthorizationCode codeRecord(final String codeValue, final OidcAuthorizationCode.Status status) {
        final var code = new OidcAuthorizationCode();
        code.setCode(codeValue);
        code.setClientId(CLIENT_ID);
        code.setRedirectUri(REDIRECT_URI);
        code.setScope("openid profile");
        code.setState("state-123");
        code.setStatus(status);
        return code;
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

    @Test
    public void testSubmitLoginWithoutConsentRedirectsImmediately() {

        when(oidcProviderDao.createAuthorizationCode(any())).then(i -> i.getArgument(0));
        when(oidcProviderDao.completeAuthorizationCode(anyString(), eq("user-1"), anyLong()))
                .then(i -> Optional.of(codeRecord(i.getArgument(0), OidcAuthorizationCode.Status.AWAITING_CONSENT)));
        when(oidcProviderDao.decideConsent(anyString(), eq(true)))
                .then(i -> Optional.of(codeRecord(i.getArgument(0), OidcAuthorizationCode.Status.PENDING)));

        getService().beginAuthorization(validRequest());
        // The code value issued by the (mocked) DAO is unknown; capture it for the login submission.
        final var created = org.mockito.ArgumentCaptor.forClass(OidcAuthorizationCode.class);
        verify(oidcProviderDao).createAuthorizationCode(created.capture());

        final var consent = getService().submitLogin(loginRequest(created.getValue().getCode()));

        assertFalse(consent.isConsentRequired());
        assertEquals(consent.getRedirectUri(), REDIRECT_URI + "?code=" + created.getValue().getCode()
                + "&state=state-123");

    }

    @Test
    public void testSubmitLoginRequiresConsentWhenConfigured() {

        final var consentedService = newConsentRequiredService();

        when(oidcProviderDao.completeAuthorizationCode(eq("the-code"), eq("user-1"), anyLong()))
                .then(i -> Optional.of(codeRecord("the-code", OidcAuthorizationCode.Status.AWAITING_CONSENT)));

        final var consent = consentedService.submitLogin(loginRequest("the-code"));

        assertTrue(consent.isConsentRequired());
        assertEquals(consent.getAuthorizationId(), "the-code");
        assertEquals(consent.getClientDisplayName(), "Test client");
        assertEquals(consent.getScope(), "openid profile");

    }

    @Test
    public void testApproveConsentCompletesAndRedirects() {

        final var consentedService = newConsentRequiredService();

        when(oidcProviderDao.decideConsent("the-code", true))
                .thenReturn(Optional.of(codeRecord("the-code", OidcAuthorizationCode.Status.PENDING)));

        final var redirect = consentedService.approveConsent("the-code");

        assertEquals(redirect, REDIRECT_URI + "?code=the-code&state=state-123");

    }

    @Test
    public void testDenyConsentRedirectsWithAccessDenied() {

        final var consentedService = newConsentRequiredService();

        when(oidcProviderDao.decideConsent("the-code", false))
                .thenReturn(Optional.of(codeRecord("the-code", OidcAuthorizationCode.Status.DENIED)));

        final var redirect = consentedService.denyConsent("the-code");

        assertEquals(redirect,
                REDIRECT_URI + "?error=access_denied&state=state-123");

    }

    @Test
    public void testConsentDecisionIsSingleUse() {
        final var consentedService = newConsentRequiredService();
        when(oidcProviderDao.decideConsent(anyString(), eq(true))).thenReturn(Optional.empty());
        assertThrows(InvalidDataException.class, () -> consentedService.approveConsent("the-code"));
    }

    @Test
    public void testSubmitLoginRejectsUnknownOrConsumedAuthorization() {
        when(oidcProviderDao.completeAuthorizationCode(anyString(), anyString(), anyLong()))
                .thenReturn(Optional.empty());
        assertThrows(InvalidDataException.class, () -> getService().submitLogin(loginRequest("bogus")));
    }

    @Test
    public void testRedirectUriWithExistingQueryUsesAmpersand() {

        when(oidcProviderDao.completeAuthorizationCode(anyString(), anyString(), anyLong()))
                .then(i -> Optional.of(codeRecord(i.getArgument(0), OidcAuthorizationCode.Status.AWAITING_CONSENT)));
        when(oidcProviderDao.decideConsent(anyString(), eq(true)))
                .then(i -> {
                    final var code = codeRecord(i.getArgument(0), OidcAuthorizationCode.Status.PENDING);
                    code.setRedirectUri(REDIRECT_URI + "?foo=bar");
                    return Optional.of(code);
                });

        final var consent = getService().submitLogin(loginRequest("auth-id"));

        assertEquals(consent.getRedirectUri(),
                REDIRECT_URI + "?foo=bar&code=auth-id&state=state-123");

    }

    /**
     * Builds a service instance configured with consent required, for the consent-flow tests.
     */
    private StandardOidcProviderAuthorizationService newConsentRequiredService() {
        final var service = createInjector(true)
                .getInstance(StandardOidcProviderAuthorizationService.class);
        return service;
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

    public StandardOidcProviderAuthorizationService getService() {
        return service;
    }

}
