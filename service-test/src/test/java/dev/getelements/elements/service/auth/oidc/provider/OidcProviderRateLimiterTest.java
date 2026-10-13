package dev.getelements.elements.service.auth.oidc.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;
import dev.getelements.elements.sdk.model.exception.RateLimitedException;
import dev.getelements.elements.sdk.service.Constants;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for the provider's fixed-window rate limiter: limits are enforced per client, buckets are
 * isolated per key and per endpoint category, and disabling the limiter admits everything.
 */
public class OidcProviderRateLimiterTest {

    @Inject
    private OidcProviderRateLimiter limiter;

    @BeforeMethod
    public void setup() {
        // A fresh instance per test method: counters are in-memory state on the instance itself.
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(boolean.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_ENABLED)).toInstance(true);
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_TOKEN_PER_MINUTE)).toInstance(3L);
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_AUTHORIZE_PER_MINUTE)).toInstance(2L);
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_LOGIN_PER_MINUTE)).toInstance(1L);
                bind(OidcProviderRateLimiter.class);
            }
        }).injectMembers(this);
    }

    @Test
    public void testTokenEndpointAdmitsUpToTheLimitThenRejects() {

        for (int i = 0; i < 3; i++) {
            limiter.checkTokenEndpoint("el_client");
        }

        org.testng.Assert.assertThrows(RateLimitedException.class,
                () -> limiter.checkTokenEndpoint("el_client"));

    }

    @Test
    public void testBucketsAreIsolatedPerClient() {

        // Exhaust one client's budget entirely.
        for (int i = 0; i < 3; i++) {
            limiter.checkTokenEndpoint("el_busy");
        }

        // A different client is unaffected.
        limiter.checkTokenEndpoint("el_other");

    }

    @Test
    public void testBucketsAreIsolatedPerEndpoint() {

        // The authorize budget is its own bucket: exhausting the token budget does not consume it.
        for (int i = 0; i < 3; i++) {
            limiter.checkTokenEndpoint("el_client");
        }

        limiter.checkAuthorizationEndpoint("el_client");

        // And the authorize budget enforces its own, smaller limit.
        limiter.checkAuthorizationEndpoint("el_client");
        org.testng.Assert.assertThrows(RateLimitedException.class,
                () -> limiter.checkAuthorizationEndpoint("el_client"));

    }

    @Test
    public void testHostedLoginLimitEnforced() {

        limiter.checkHostedLogin("auth-1", "user");
        org.testng.Assert.assertThrows(RateLimitedException.class,
                () -> limiter.checkHostedLogin("auth-1", "user"));

        // A different authorization (and a different username) still gets its own slot.
        limiter.checkHostedLogin("auth-2", "other-user");

    }

    @Test
    public void testDisabledLimiterAdmitsEverything() {

        final var disabled = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(boolean.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_ENABLED)).toInstance(false);
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_TOKEN_PER_MINUTE)).toInstance(1L);
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_AUTHORIZE_PER_MINUTE)).toInstance(1L);
                bind(long.class).annotatedWith(Names.named(Constants.OIDC_RATE_LIMIT_LOGIN_PER_MINUTE)).toInstance(1L);
                bind(OidcProviderRateLimiter.class);
            }
        }).getInstance(OidcProviderRateLimiter.class);

        for (int i = 0; i < 10; i++) {
            disabled.checkTokenEndpoint("el_client");
        }

    }

    @Test
    public void testUnkeyedRequestsAreNotCounted() {
        // Requests carrying no client id cannot be attributed to a bucket; the limiter admits them (the
        // endpoint's own validation rejects them later). Documented behavior, not an oversight.
        for (int i = 0; i < 10; i++) {
            limiter.checkTokenEndpoint(null);
            limiter.checkTokenEndpoint("");
        }
    }

}
