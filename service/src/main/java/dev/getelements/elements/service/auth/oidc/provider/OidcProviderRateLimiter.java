package dev.getelements.elements.service.auth.oidc.provider;

import dev.getelements.elements.sdk.model.exception.RateLimitedException;
import dev.getelements.elements.sdk.service.Constants;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;

/**
 * In-process, fixed-window rate limiting for the OIDC provider's authorization, token, and hosted-login
 * endpoints, keyed by client id. Counters are held per one-minute window and expire lazily; a request that
 * pushes a window past its configured limit raises {@link RateLimitedException} (HTTP 429).
 *
 * <p>The counters are per-JVM: deployments serving the provider from multiple nodes must treat these limits
 * as per-node rather than global. There is no IP-based keying — the codebase has no remote-address handling —
 * so clients behind a shared NAT share a bucket. Both are deliberate 135b simplifications.</p>
 */
@Singleton
public class OidcProviderRateLimiter {

    private static final long WINDOW_MILLIS = 60_000;

    /** Retires closed windows once this many are held, bounding memory under key churn. */
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    @Inject
    @Named(Constants.OIDC_RATE_LIMIT_ENABLED)
    private boolean enabled;

    @Inject
    @Named(Constants.OIDC_RATE_LIMIT_TOKEN_PER_MINUTE)
    private long tokenPerMinute;

    @Inject
    @Named(Constants.OIDC_RATE_LIMIT_AUTHORIZE_PER_MINUTE)
    private long authorizePerMinute;

    @Inject
    @Named(Constants.OIDC_RATE_LIMIT_LOGIN_PER_MINUTE)
    private long loginPerMinute;

    /**
     * Admits one token endpoint request for the supplied client.
     *
     * @param clientId the presented client id, possibly null when absent from the request
     */
    public void checkTokenEndpoint(final String clientId) {
        check("token", tokenPerMinute, clientId);
    }

    /**
     * Admits one authorization endpoint request for the supplied client.
     *
     * @param clientId the presented client id, possibly null when absent from the request
     */
    public void checkAuthorizationEndpoint(final String clientId) {
        check("authorize", authorizePerMinute, clientId);
    }

    /**
     * Admits one hosted-login submission against the supplied pending authorization and username, guarding
     * the credential-entry step against brute force.
     *
     * @param authorizationId the pending authorization's id
     * @param username the submitted username
     */
    public void checkHostedLogin(final String authorizationId, final String username) {
        check("login", loginPerMinute, authorizationId);
        check("login-user", loginPerMinute, username);
    }

    private void check(final String category, final long limit, final String key) {

        if (!enabled || limit <= 0 || key == null || key.isBlank()) {
            return;
        }

        final var now = currentTimeMillis();
        final var windowIndex = now / WINDOW_MILLIS;
        final var windowKey = category + ':' + key + ':' + windowIndex;

        final var counter = counters.computeIfAbsent(windowKey, k -> new Counter());
        final var count = counter.count.incrementAndGet();

        if (count > limit) {
            final var retrySeconds = Math.max(1, ((windowIndex + 1) * WINDOW_MILLIS - now) / 1000);
            throw new RateLimitedException("Rate limit exceeded; retry in " + retrySeconds + " seconds.");
        }

        if (counters.size() > CLEANUP_THRESHOLD) {
            counters.keySet().removeIf(k -> !k.equals(windowKey) && isStale(k, windowIndex));
        }

    }

    /**
     * A key's window index is embedded as its final colon-separated segment; anything older than the current
     * window can no longer be incremented by a live request.
     */
    private boolean isStale(final String key, final long currentWindowIndex) {
        final var separator = key.lastIndexOf(':');
        if (separator < 0) {
            return true;
        }
        try {
            return Long.parseLong(key.substring(separator + 1)) < currentWindowIndex;
        } catch (final NumberFormatException ex) {
            return true;
        }

    }

    private static class Counter {
        private final AtomicLong count = new AtomicLong();
    }

}
