package dev.getelements.elements.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.OidcAuthSchemeDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.service.auth.oidc.OidcAuthServiceOperations;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static dev.getelements.elements.sdk.service.Constants.OIDC_JWKS_REFRESH_SECONDS;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Covers the auto-create-primary-profile gap fixed for OIDC logins: {@link OidcSessionRequest#getApplicationNameOrId()}
 * should behave like the equivalent OAuth2 field, gated on {@code Application#getAutoCreateProfile()}/
 * {@code Application#getMaxProfiles()}, while the legacy JWT {@code aud}-claim-driven path (no request-level field)
 * must keep using the ungated {@code createOrRefreshProfile} exactly as before.
 */
public class OidcAuthServiceOperationsProfileTest {

    private static final String ISSUER = "https://issuer.test";
    private static final String KID = "test-kid";

    @Inject
    private OidcAuthServiceOperations ops;

    @Inject
    private OidcAuthSchemeDao schemeDao;

    @Inject
    private ApplicationDao applicationDao;

    @Inject
    private ProfileDao profileDao;

    @Inject
    private SessionDao sessionDao;

    private RSAPublicKey publicKey;

    private Algorithm algorithm;

    @BeforeMethod
    public void setup() throws Exception {

        createInjector(new TestModule()).injectMembers(this);
        when(sessionDao.create(any(Session.class))).thenReturn(new SessionCreation());

        final var kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        final var kp = kpg.generateKeyPair();

        publicKey = (RSAPublicKey) kp.getPublic();
        algorithm = Algorithm.RSA256(publicKey, (RSAPrivateKey) kp.getPrivate());

    }

    private OidcAuthScheme scheme() {

        final var n = Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray());
        final var e = Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray());
        final var jwk = new JWK("RS256", KID, "RSA", "sig", e, n);

        final var scheme = new OidcAuthScheme();
        scheme.setIssuer(ISSUER);
        scheme.setKeys(List.of(jwk));

        return scheme;

    }

    private String token(final String audience) {

        var builder = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("test-subject")
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000));

        if (audience != null) {
            builder = builder.withAudience(audience);
        }

        return builder.sign(algorithm);

    }

    @SuppressWarnings("unchecked")
    private BiFunction<com.auth0.jwt.interfaces.DecodedJWT, OidcAuthScheme, User> userMapper(final User user) {
        final BiFunction<com.auth0.jwt.interfaces.DecodedJWT, OidcAuthScheme, User> mapper = mock(BiFunction.class);
        when(mapper.apply(any(), any())).thenReturn(user);
        return mapper;
    }

    @Test
    public void testAutoCreatesPrimaryProfileWhenRequestedAndConfigured() {

        when(schemeDao.findAuthScheme(ISSUER)).thenReturn(Optional.of(scheme()));

        final var req = new OidcSessionRequest();
        req.setJwt(token(null));
        req.setApplicationNameOrId("app-1");

        final var user = new User();
        user.setId("user-1");

        final var application = new Application();
        application.setId("app-1");
        application.setAutoCreateProfile(true);
        application.setMaxProfiles(1);

        when(applicationDao.findActiveApplication("app-1")).thenReturn(Optional.of(application));
        when(profileDao.findPrimaryProfile("user-1", "app-1")).thenReturn(Optional.empty());

        final var createdProfile = new Profile();
        createdProfile.setId("profile-1");
        when(profileDao.createSlottedProfile(any(Profile.class), anyMap())).thenReturn(createdProfile);

        ops.createOrUpdateUserWithToken(req, userMapper(user));

        final var profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileDao).createSlottedProfile(profileCaptor.capture(), anyMap());
        assertEquals(profileCaptor.getValue().getUser(), user);
        assertEquals(profileCaptor.getValue().getApplication(), application);
        verify(profileDao, never()).createOrRefreshProfile(any());

        final var sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertEquals(sessionCaptor.getValue().getProfile(), createdProfile);
        assertEquals(sessionCaptor.getValue().getApplication(), application);
    }

    @Test
    public void testDoesNotAutoCreatePrimaryProfileWhenRequestedButNotConfigured() {

        when(schemeDao.findAuthScheme(ISSUER)).thenReturn(Optional.of(scheme()));

        final var req = new OidcSessionRequest();
        req.setJwt(token(null));
        req.setApplicationNameOrId("app-2");

        final var user = new User();
        user.setId("user-2");

        final var application = new Application();
        application.setId("app-2");
        application.setAutoCreateProfile(false);
        application.setMaxProfiles(1);

        when(applicationDao.findActiveApplication("app-2")).thenReturn(Optional.of(application));
        when(profileDao.findPrimaryProfile("user-2", "app-2")).thenReturn(Optional.empty());

        ops.createOrUpdateUserWithToken(req, userMapper(user));

        verify(profileDao, never()).createSlottedProfile(any(), anyMap());
        verify(profileDao, never()).createOrRefreshProfile(any());

        final var sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertNull(sessionCaptor.getValue().getProfile());
        assertNull(sessionCaptor.getValue().getApplication());
    }

    @Test
    public void testLegacyAudClaimPathStillUsesUngatedCreateOrRefresh() {

        when(schemeDao.findAuthScheme(ISSUER)).thenReturn(Optional.of(scheme()));

        final var req = new OidcSessionRequest();
        req.setJwt(token("app-3")); // aud claim drives resolution; no applicationNameOrId on the request

        final var user = new User();
        user.setId("user-3");

        final var application = new Application();
        application.setId("app-3");
        application.setAutoCreateProfile(false); // deliberately not configured -- legacy path ignores this
        application.setMaxProfiles(0);

        when(applicationDao.findActiveApplication("app-3")).thenReturn(Optional.of(application));
        when(profileDao.findPrimaryProfile("user-3", "app-3")).thenReturn(Optional.empty());

        final var createdProfile = new Profile();
        createdProfile.setId("profile-3");
        when(profileDao.createOrRefreshProfile(any(Profile.class))).thenReturn(createdProfile);

        ops.createOrUpdateUserWithToken(req, userMapper(user));

        verify(profileDao).createOrRefreshProfile(any(Profile.class));
        verify(profileDao, never()).createSlottedProfile(any(), anyMap());

        final var sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertEquals(sessionCaptor.getValue().getProfile(), createdProfile);
    }

    private static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(OidcAuthSchemeDao.class).toInstance(mock(OidcAuthSchemeDao.class));
            bind(SessionDao.class).toInstance(mock(SessionDao.class));
            bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
            bind(NameService.class).toInstance(mock(NameService.class));
            bind(ApplicationDao.class).toInstance(mock(ApplicationDao.class));
            bind(Client.class).toInstance(mock(Client.class));
            bindConstant().annotatedWith(Names.named(SESSION_TIMEOUT_SECONDS)).to(3600L);
            bindConstant().annotatedWith(Names.named(OIDC_JWKS_REFRESH_SECONDS)).to(3600L);
            bind(String.class).annotatedWith(named(API_OUTSIDE_URL)).toInstance("http://localhost:8080/api/rest");
        }
    }

}
