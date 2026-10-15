package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.auth.OidcProviderGrant;
import dev.getelements.elements.sdk.model.auth.OidcRefreshToken;
import dev.getelements.elements.sdk.mongo.test.DockerMongoTestInstance;
import dev.getelements.elements.sdk.mongo.test.MongoTestInstance;import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.mongo.guice.MongoSdkModule;
import dev.getelements.elements.dao.mongo.guice.MongoDatastoreBootstrapModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoMigrationModule;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import jakarta.inject.Inject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import ru.vyarus.guice.validator.ValidationModule;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Verifies the atomic single-use semantics of the OIDC provider's server-side state: authorization codes are
 * redeemable exactly once, refresh tokens are consumable exactly once, grants rotate off their old session
 * exactly once, and expired-but-not-yet-swept records are rejected on read.
 */
@Guice(modules = MongoOidcProviderDaoIntegrationTest.TestModule.class)
public class MongoOidcProviderDaoIntegrationTest {

    public static final String TEST_COMPONENT = "dev.getelements.elements.dao.mongo.test.MongoOidcProviderDaoIntegrationTest";

    private static final AtomicInteger testPort = new AtomicInteger(46300);

    @Inject
    private OidcProviderDao oidcProviderDao;

    private OidcClientRegistration registration;

    @BeforeClass
    public void setupRegistration() {
        registration = new OidcClientRegistration();
        registration.setClientId("el_it_client");
        registration.setDisplayName("Integration test client");
        registration.setRedirectUris(Set.of("https://client.example.com/callback"));
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.AUTHORIZATION_CODE,
                OidcClientRegistration.GrantType.REFRESH_TOKEN));
        registration.setScopes(Set.of("openid", "profile", "email"));
        registration.setStatus(OidcClientRegistration.Status.ACTIVE);
        registration.setClientSecret("integration-test-secret");
    }

    @Test
    public void testClientRegistrationRoundTrip() {

        final var created = getOidcProviderDao().createClient(registration);
        assertEquals(created.getClientId(), "el_it_client");

        // The write-only secret never round-trips.
        assertEquals(created.getClientSecret(), null);

        final var fetched = getOidcProviderDao().getClient("el_it_client").orElseThrow();
        assertEquals(fetched.getClientId(), "el_it_client");
        assertEquals(fetched.getRedirectUris(), registration.getRedirectUris());
        assertEquals(fetched.getStatus(), OidcClientRegistration.Status.ACTIVE);

        // A duplicate registration is rejected.
        final var duplicate = new OidcClientRegistration();
        duplicate.setClientId("el_it_client");
        duplicate.setRedirectUris(Set.of("https://x"));
        duplicate.setGrantTypes(Set.of(OidcClientRegistration.GrantType.AUTHORIZATION_CODE));
        duplicate.setScopes(Set.of("openid"));

        boolean conflict = false;
        try {
            getOidcProviderDao().createClient(duplicate);
        } catch (RuntimeException ex) {
            conflict = true;
        }
        assertTrue(conflict, "Duplicate clientId must be rejected.");

    }

    @Test
    public void testAuthorizationCodeRedeemableExactlyOnce() {

        final var code = new OidcAuthorizationCode();
        code.setCode("code-single-use");
        code.setClientId("el_it_client");
        code.setUserId("user-1");
        code.setRedirectUri("https://client.example.com/callback");
        code.setScope("openid");
        code.setStatus(OidcAuthorizationCode.Status.PENDING);
        code.setExpiry(new Timestamp(System.currentTimeMillis() + 600_000));

        getOidcProviderDao().createAuthorizationCode(code);

        final var redeemed = getOidcProviderDao().redeemAuthorizationCode("code-single-use");
        assertTrue(redeemed.isPresent());
        assertEquals(redeemed.get().getUserId(), "user-1");

        // The replay fails closed.
        assertTrue(getOidcProviderDao().redeemAuthorizationCode("code-single-use").isEmpty());

        // Unknown codes fail.
        assertTrue(getOidcProviderDao().redeemAuthorizationCode("code-unknown").isEmpty());

    }

    @Test
    public void testAuthorizationCodeExpiredIsRejectedDespiteTtlSweepLag() {

        final var code = new OidcAuthorizationCode();
        code.setCode("code-expired");
        code.setClientId("el_it_client");
        code.setUserId("user-1");
        code.setRedirectUri("https://client.example.com/callback");
        code.setScope("openid");
        code.setStatus(OidcAuthorizationCode.Status.PENDING);
        code.setExpiry(new Timestamp(System.currentTimeMillis() - 1000));

        getOidcProviderDao().createAuthorizationCode(code);

        // Mongo's TTL sweep is asynchronous; the redemption query filters on expiry so an
        // expired-but-not-yet-swept code is still rejected.
        assertTrue(getOidcProviderDao().redeemAuthorizationCode("code-expired").isEmpty());

    }

    @Test
    public void testCompleteAuthorizationCodeAttachesUserExactlyOnce() {

        final var code = new OidcAuthorizationCode();
        code.setCode("code-awaiting-login");
        code.setClientId("el_it_client");
        code.setRedirectUri("https://client.example.com/callback");
        code.setScope("openid");
        code.setStatus(OidcAuthorizationCode.Status.PENDING);
        code.setExpiry(new Timestamp(System.currentTimeMillis() + 600_000));

        getOidcProviderDao().createAuthorizationCode(code);

        final var completed = getOidcProviderDao().completeAuthorizationCode(
                "code-awaiting-login", "user-1", System.currentTimeMillis());
        assertTrue(completed.isPresent());
        assertEquals(completed.get().getUserId(), "user-1");
        // The code awaits the (explicit or implicit) consent decision before it may be redeemed.
        assertEquals(completed.get().getStatus(), OidcAuthorizationCode.Status.AWAITING_CONSENT);

        // A second completion attempt (double-submitted form) fails closed.
        assertTrue(getOidcProviderDao()
                .completeAuthorizationCode("code-awaiting-login", "user-2", System.currentTimeMillis())
                .isEmpty());

        // Approval restores PENDING so the token endpoint can redeem it.
        final var approved = getOidcProviderDao().decideConsent("code-awaiting-login", true);
        assertTrue(approved.isPresent());
        assertEquals(approved.get().getStatus(), OidcAuthorizationCode.Status.PENDING);
        assertEquals(approved.get().getUserId(), "user-1");

        assertTrue(getOidcProviderDao().redeemAuthorizationCode("code-awaiting-login").isPresent());

    }

    @Test
    public void testConsentDenialIsFinalAndSingleUse() {

        final var code = new OidcAuthorizationCode();
        code.setCode("code-consent-denied");
        code.setClientId("el_it_client");
        code.setRedirectUri("https://client.example.com/callback");
        code.setScope("openid");
        code.setStatus(OidcAuthorizationCode.Status.PENDING);
        code.setExpiry(new Timestamp(System.currentTimeMillis() + 600_000));

        getOidcProviderDao().createAuthorizationCode(code);

        getOidcProviderDao().completeAuthorizationCode("code-consent-denied", "user-1", System.currentTimeMillis());

        final var denied = getOidcProviderDao().decideConsent("code-consent-denied", false);
        assertTrue(denied.isPresent());
        assertEquals(denied.get().getStatus(), OidcAuthorizationCode.Status.DENIED);

        // A denied code is unredeemable, and the decision cannot be revisited.
        assertTrue(getOidcProviderDao().redeemAuthorizationCode("code-consent-denied").isEmpty());
        assertTrue(getOidcProviderDao().decideConsent("code-consent-denied", true).isEmpty());
        assertTrue(getOidcProviderDao().decideConsent("code-consent-denied", false).isEmpty());

    }

    @Test
    public void testConsentDecisionRequiresAwaitingConsentState() {

        final var code = new OidcAuthorizationCode();
        code.setCode("code-never-logged-in");
        code.setClientId("el_it_client");
        code.setRedirectUri("https://client.example.com/callback");
        code.setScope("openid");
        code.setStatus(OidcAuthorizationCode.Status.PENDING);
        code.setExpiry(new Timestamp(System.currentTimeMillis() + 600_000));

        getOidcProviderDao().createAuthorizationCode(code);

        // A PENDING code with no authenticated user cannot be consented to — there is no user to consent.
        assertTrue(getOidcProviderDao().decideConsent("code-never-logged-in", true).isEmpty());

    }

    @Test
    public void testGrantRotationIsAtomicAndSingleUse() {

        final var grant = new OidcProviderGrant();
        grant.setClientId("el_it_client");
        grant.setUserId("user-1");
        grant.setScope("openid");
        grant.setSessionId("session-a");
        grant.setExpiry(new Timestamp(System.currentTimeMillis() + 600_000));

        final var created = getOidcProviderDao().createGrant(grant);
        assertNotNull(created.getId());

        final var rotated = getOidcProviderDao().rotateGrantSession(created.getId(), "session-a", "session-b");
        assertTrue(rotated.isPresent());
        assertEquals(rotated.get().getSessionId(), "session-b");

        // The old session no longer matches — a concurrent rotation with stale state fails.
        assertTrue(getOidcProviderDao().rotateGrantSession(created.getId(), "session-a", "session-c").isEmpty());

        // Lookup by the new session id succeeds; lookup by the old fails.
        assertTrue(getOidcProviderDao().findGrantBySessionId("session-b").isPresent());
        assertTrue(getOidcProviderDao().findGrantBySessionId("session-a").isEmpty());

    }

    @Test
    public void testRefreshTokenConsumableExactlyOnce() {

        final var grant = new OidcProviderGrant();
        grant.setClientId("el_it_client");
        grant.setUserId("user-1");
        grant.setScope("openid");
        grant.setSessionId("session-rt");
        grant.setExpiry(new Timestamp(System.currentTimeMillis() + 600_000));

        final var createdGrant = getOidcProviderDao().createGrant(grant);

        final var token = new OidcRefreshToken();
        token.setSecretHash("hash-refresh-single-use");
        token.setGrantId(createdGrant.getId());
        token.setClientId("el_it_client");
        token.setStatus(OidcRefreshToken.Status.ACTIVE);
        token.setExpiry(createdGrant.getExpiry());

        getOidcProviderDao().createRefreshToken(token);

        final var consumed = getOidcProviderDao().consumeRefreshToken("hash-refresh-single-use");
        assertTrue(consumed.isPresent());
        assertEquals(consumed.get().getStatus(), OidcRefreshToken.Status.CONSUMED);

        // Replay fails closed.
        assertTrue(getOidcProviderDao().consumeRefreshToken("hash-refresh-single-use").isEmpty());

    }

    @Test
    public void testRevokeRefreshTokensForGrant() {

        final var grant = new OidcProviderGrant();
        grant.setClientId("el_it_client");
        grant.setUserId("user-1");
        grant.setScope("openid");
        grant.setSessionId("session-revoke");
        grant.setExpiry(new Timestamp(System.currentTimeMillis() + 600_000));

        final var createdGrant = getOidcProviderDao().createGrant(grant);

        for (int i = 0; i < 3; i++) {
            final var token = new OidcRefreshToken();
            token.setSecretHash("hash-revoke-" + i);
            token.setGrantId(createdGrant.getId());
            token.setClientId("el_it_client");
            token.setStatus(OidcRefreshToken.Status.ACTIVE);
            token.setExpiry(createdGrant.getExpiry());
            getOidcProviderDao().createRefreshToken(token);
        }

        getOidcProviderDao().revokeRefreshTokensForGrant(createdGrant.getId());

        assertTrue(getOidcProviderDao().consumeRefreshToken("hash-revoke-0").isEmpty());
        assertTrue(getOidcProviderDao().consumeRefreshToken("hash-revoke-1").isEmpty());
        assertTrue(getOidcProviderDao().consumeRefreshToken("hash-revoke-2").isEmpty());

    }

    public OidcProviderDao getOidcProviderDao() {
        return oidcProviderDao;
    }

    public static class TestModule extends com.google.inject.AbstractModule {

        private static final AtomicInteger portCounter = testPort;

        @Override
        protected void configure() {

            final var defaultConfigurationSupplier =
                    new dev.getelements.elements.config.DefaultConfigurationSupplier();
            final int port = portCounter.getAndIncrement();
            final String uri = format("mongodb://localhost:%d", port);

            final var mongoTestInstance = new DockerMongoTestInstance(port);
            mongoTestInstance.start();
            java.lang.Runtime.getRuntime().addShutdownHook(new Thread(mongoTestInstance::close));

            install(new ConfigurationModule(() -> {
                final var properties = defaultConfigurationSupplier.get();
                properties.put(MongoConfigurationService.MONGO_CLIENT_URI, uri);
                return properties;
            }));

            install(new MongoDatastoreBootstrapModule());
            install(new MongoSdkModule());
            install(new MongoDaoModule());
            install(new MongoMigrationModule());
            install(new ValidationModule());

        }

    }

}
