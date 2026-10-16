package dev.getelements.elements.service.auth.oidc;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.OidcAuthSchemeDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.OidcAuthLoginEvent;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.SessionTokenIssuer;
import dev.getelements.elements.sdk.service.name.NameService;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.getelements.elements.sdk.service.auth.OidcAuthService.OIDC_AUTH_LOGIN_EVENT;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

/**
 * Covers the {@code OIDC_AUTH_LOGIN_EVENT} contract (#93). Signs real RS256 id_tokens against a generated
 * keypair so the signature-verification path is exercised for real, across the flow permutations: the
 * direct id_token login (anon and account-link differ only in the mapper, whose resolved user the event
 * must carry), profile resolution (none requested, unknown application, existing primary profile), provider
 * threading on the login-attempt paths, the token-less confirm flow, and the failure paths (unknown scheme,
 * issuer mismatch, expired token, bad signature), which must publish nothing.
 */
public class OidcAuthServiceOperationsTest {

    private static final String ISSUER = "https://issuer.example.com";

    private static final String KID = "test-key";

    private OidcAuthSchemeDao oidcAuthSchemeDao;

    private ProfileDao profileDao;

    private ApplicationDao applicationDao;

    private SessionTokenIssuer sessionTokenIssuer;

    private MutableElementRegistry elementRegistry;

    private List<Event> publishedEvents;

    private OidcAuthServiceOperations operations;

    private KeyPair keyPair;

    private OidcAuthScheme scheme;

    @BeforeMethod
    public void setup() throws Exception {

        keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        scheme = new OidcAuthScheme();
        scheme.setId("scheme-1");
        scheme.setName("scheme-1");
        scheme.setIssuer(ISSUER);
        scheme.setKeys(List.of(jwk()));
        scheme.setKeysFetchedAt(currentTimeMillis() / 1000); // fresh, so no refresh is attempted

        oidcAuthSchemeDao = mock(OidcAuthSchemeDao.class);
        when(oidcAuthSchemeDao.findAuthScheme(ISSUER)).thenReturn(java.util.Optional.of(scheme));

        profileDao = mock(ProfileDao.class);
        applicationDao = mock(ApplicationDao.class);
        sessionTokenIssuer = mock(SessionTokenIssuer.class);
        mock(NameService.class);
        mock(Client.class);

        elementRegistry = MutableElementRegistry.newDefaultInstance();
        publishedEvents = new CopyOnWriteArrayList<>();
        elementRegistry.onEvent(ev -> {
            if (OIDC_AUTH_LOGIN_EVENT.equals(ev.getEventName())) {
                publishedEvents.add(ev);
            }
        });

        operations = new OidcAuthServiceOperations();
        operations.setOidcAuthSchemeDao(oidcAuthSchemeDao);
        operations.setProfileDao(profileDao);
        operations.setApplicationDao(applicationDao);
        operations.setSessionTokenIssuer(sessionTokenIssuer);
        operations.setNameService(mock(NameService.class));
        operations.setElementRegistry(elementRegistry);
        operations.setSessionTimeoutSeconds(60);

    }

    @AfterMethod
    public void closeRegistry() {
        elementRegistry.close();
    }

    // -- Good input: the direct id_token login (anon and account-link differ only in the mapper) -----

    @Test
    public void directTokenLoginWithoutApplicationPublishesEventWithoutProfile() {

        final var rawToken = signToken("external-1");
        final var sessionCreation = createOrUpdateUser(rawToken, null, "user-1");

        assertNotNull(sessionCreation);
        assertEquals(publishedEvents.size(), 1, "exactly one login event must be published");

        final var event = publishedEvents.get(0).getEventArgument(0, OidcAuthLoginEvent.class);
        assertEquals(event.user().getId(), "user-1", "the event must carry the mapper-resolved user");
        assertNull(event.profile(), "no application requested, so no profile");
        assertEquals(event.idToken(), rawToken, "the event must carry the raw, validated id_token");
        assertEquals(event.scheme(), scheme);
        assertNull(event.provider(), "the direct id_token path resolves no provider configuration");

    }

    @Test
    public void directTokenLoginWithExistingPrimaryProfilePublishesEventWithProfile() {

        final var application = application("application-1");
        when(applicationDao.findActiveApplication("application-1")).thenReturn(java.util.Optional.of(application));

        final var profile = profile("profile-1");
        when(profileDao.findPrimaryProfile("user-1", "application-1")).thenReturn(java.util.Optional.of(profile));

        createOrUpdateUser(signToken("external-1"), "application-1", "user-1");

        assertEquals(publishedEvents.size(), 1);
        assertEquals(publishedEvents.get(0).getEventArgument(0, OidcAuthLoginEvent.class).profile(), profile);
    }

    @Test
    public void directTokenLoginWithUnknownApplicationPublishesEventWithoutProfile() {
        when(applicationDao.findActiveApplication("application-1")).thenReturn(java.util.Optional.empty());

        createOrUpdateUser(signToken("external-1"), "application-1", "user-1");

        assertEquals(publishedEvents.size(), 1);
        assertNull(publishedEvents.get(0).getEventArgument(0, OidcAuthLoginEvent.class).profile());
    }

    @Test
    public void providerThreadingThroughTheLoginAttemptPathIsCarriedOnTheEvent() {

        final var provider = new OidcProviderConfiguration();
        provider.setId("provider-1");
        provider.setName("provider-1");

        when(sessionTokenIssuer.issue(any())).then(a -> issue(a.getArgument(0, Session.class)));

        final var sessionCreation = operations.createOrUpdateUserWithVerifiedToken(
                JWT.decode(signToken("external-1")), scheme, userWithId("user-1"),
                "application-1", provider);

        assertNotNull(sessionCreation);
        assertEquals(publishedEvents.size(), 1);
        assertEquals(publishedEvents.get(0).getEventArgument(0, OidcAuthLoginEvent.class).provider(), provider,
                "the login-attempt callback path must carry the provider configuration");
    }

    // -- The token-less confirm flow (browser-redirect account linking) ------------------------------

    @Test
    public void confirmFlowPublishesEventWithoutTokenOrScheme() {

        when(sessionTokenIssuer.issue(any())).then(a -> issue(a.getArgument(0, Session.class)));

        final var sessionCreation = operations.createSessionForResolvedUser(userWithId("user-1").apply(null, null));

        assertNotNull(sessionCreation);
        assertEquals(publishedEvents.size(), 1);

        final var event = publishedEvents.get(0).getEventArgument(0, OidcAuthLoginEvent.class);
        assertEquals(event.user().getId(), "user-1");
        assertNull(event.profile());
        assertNull(event.idToken(), "the confirm flow has no live token");
        assertNull(event.scheme());
        assertNull(event.provider());

    }

    @Test
    public void confirmFlowCarriesTheResolvedProvider() {

        final var provider = new OidcProviderConfiguration();
        provider.setId("provider-1");

        when(sessionTokenIssuer.issue(any())).then(a -> issue(a.getArgument(0, Session.class)));

        operations.createSessionForResolvedUser(userWithId("user-1").apply(null, null), null, false, provider);

        assertEquals(publishedEvents.size(), 1);
        assertEquals(publishedEvents.get(0).getEventArgument(0, OidcAuthLoginEvent.class).provider(), provider);
    }

    // -- Bad input: every failure mode must publish nothing ------------------------------------------

    @Test
    public void unknownSchemeDoesNotPublishEvent() {
        when(oidcAuthSchemeDao.findAuthScheme(ISSUER)).thenReturn(java.util.Optional.empty());

        assertLoginForbidden(signToken("external-1"));
        assertEquals(publishedEvents.size(), 0);
    }

    @Test
    public void issuerMismatchDoesNotPublishEvent() {

        final var otherIssuerToken = JWT.create()
                .withHeader(Map.of("kid", KID))
                .withIssuer("https://someone-else.example.com")
                .withClaim("sub", "external-1")
                .withExpiresAt(new Date(currentTimeMillis() + SECONDS.toMillis(300)))
                .sign(rsaAlgorithm());

        assertLoginForbidden(otherIssuerToken);
        assertEquals(publishedEvents.size(), 0);
    }

    @Test
    public void expiredTokenDoesNotPublishEvent() {

        final var expiredToken = JWT.create()
                .withHeader(Map.of("kid", KID))
                .withIssuer(ISSUER)
                .withClaim("sub", "external-1")
                .withExpiresAt(new Date(currentTimeMillis() - SECONDS.toMillis(300)))
                .sign(rsaAlgorithm());

        assertLoginForbidden(expiredToken);
        assertEquals(publishedEvents.size(), 0);
    }

    @Test
    public void badSignatureDoesNotPublishEvent() throws Exception {

        // Signed by a key that does not match the JWK published on the scheme.
        final var otherKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        final var forgedToken = JWT.create()
                .withHeader(Map.of("kid", KID))
                .withIssuer(ISSUER)
                .withClaim("sub", "external-1")
                .withExpiresAt(new Date(currentTimeMillis() + SECONDS.toMillis(300)))
                .sign(Algorithm.RSA256(
                        (java.security.interfaces.RSAPublicKey) otherKeyPair.getPublic(),
                        (java.security.interfaces.RSAPrivateKey) otherKeyPair.getPrivate()));

        assertLoginForbidden(forgedToken);
        assertEquals(publishedEvents.size(), 0);
    }

    @Test
    public void malformedTokenDoesNotPublishEvent() {

        final var request = new OidcSessionRequest();
        request.setJwt("not-a-jwt");

        // A token that cannot be decoded fails before the scheme lookup.
        expectThrows(dev.getelements.elements.sdk.model.exception.InvalidDataException.class,
                () -> operations.createOrUpdateUserWithToken(request, userWithId("user-1")));

        assertEquals(publishedEvents.size(), 0);
        verify(sessionTokenIssuer, never()).issue(any());

    }

    // -- Fixtures ------------------------------------------------------------------------------------

    private SessionCreation createOrUpdateUser(final String rawToken,
                                               final String applicationNameOrId,
                                               final String userId) {

        when(sessionTokenIssuer.issue(any())).then(a -> issue(a.getArgument(0, Session.class)));

        final var request = new OidcSessionRequest();
        request.setJwt(rawToken);
        request.setApplicationNameOrId(applicationNameOrId);

        return operations.createOrUpdateUserWithToken(request, userWithId(userId));

    }

    private void assertLoginForbidden(final String rawToken) {

        final var request = new OidcSessionRequest();
        request.setJwt(rawToken);

        final var ex = expectThrows(ForbiddenException.class,
                () -> operations.createOrUpdateUserWithToken(request, userWithId("user-1")));

        assertNotNull(ex);
        verify(sessionTokenIssuer, never()).issue(any());

    }

    private java.util.function.BiFunction<com.auth0.jwt.interfaces.DecodedJWT, OidcAuthScheme, User> userWithId(
            final String userId) {
        return (jwt, authScheme) -> {
            final var user = new User();
            user.setId(userId);
            return user;
        };
    }

    private SessionCreation issue(final Session session) {
        final var creation = new SessionCreation();
        creation.setSession(session);
        creation.setSessionSecret("secret");
        return creation;
    }

    private String signToken(final String subject) {
        return JWT.create()
                .withHeader(Map.of("kid", KID))
                .withIssuer(ISSUER)
                .withClaim("sub", subject)
                .withClaim("email", subject + "@example.com")
                .withExpiresAt(new Date(currentTimeMillis() + SECONDS.toMillis(300)))
                .sign(rsaAlgorithm());
    }

    private Algorithm rsaAlgorithm() {
        return Algorithm.RSA256(
                (java.security.interfaces.RSAPublicKey) keyPair.getPublic(),
                (java.security.interfaces.RSAPrivateKey) keyPair.getPrivate());
    }

    private JWK jwk() {

        final var publicKey = (RSAPublicKey) keyPair.getPublic();
        final var encoder = Base64.getUrlEncoder();

        final var jwk = new JWK();
        jwk.setKid(KID);
        jwk.setKty("RSA");
        jwk.setAlg("RS256");
        jwk.setN(encoder.encodeToString(publicKey.getModulus().toByteArray()));
        jwk.setE(encoder.encodeToString(publicKey.getPublicExponent().toByteArray()));
        return jwk;

    }

    private static Application application(final String id) {
        final var application = new Application();
        application.setId(id);
        return application;
    }

    private static Profile profile(final String id) {
        final var profile = new Profile();
        profile.setId(id);
        return profile;
    }

}
