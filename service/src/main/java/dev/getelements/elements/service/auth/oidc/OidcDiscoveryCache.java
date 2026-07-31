package dev.getelements.elements.service.auth.oidc;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.getelements.elements.sdk.service.Constants.OIDC_DISCOVERY_REFRESH_SECONDS;

/**
 * A purely in-memory, TTL-based cache of OIDC discovery documents, keyed by discovery URL. Refreshes
 * automatically once an entry goes stale, requiring no admin action to pick up a provider's endpoint changes —
 * the same automatic-refresh spirit as the existing JWKS cache-on-miss behavior in
 * {@code OidcAuthServiceOperations}, but simpler: discovery documents are lightweight and only a handful of
 * providers will ever be configured, so there is no need to persist the cache to MongoDB.
 *
 * <p>Must remain a singleton — the cache map is instance state, and each provider config re-triggers a live HTTP
 * fetch if this class is accidentally bound with a non-singleton scope.
 */
@Singleton
public class OidcDiscoveryCache {

    private record CachedDiscovery(OidcDiscoveryDocument document, Instant fetchedAt) {}

    private final Map<String, CachedDiscovery> cache = new ConcurrentHashMap<>();

    private Client client;

    private long refreshIntervalSeconds;

    /**
     * Returns the discovery document for the given URL, serving from cache if it was fetched within the last
     * {@link #getRefreshIntervalSeconds()} seconds, otherwise fetching fresh and caching the result.
     *
     * @param discoveryUrl the OIDC discovery document URL
     * @return the discovery document
     */
    public OidcDiscoveryDocument resolve(final String discoveryUrl) {

        final var cached = cache.get(discoveryUrl);

        if (cached != null && !isStale(cached)) {
            return cached.document();
        }

        final var fresh = getClient()
                .target(discoveryUrl)
                .request(MediaType.APPLICATION_JSON)
                .get(OidcDiscoveryDocument.class);

        cache.put(discoveryUrl, new CachedDiscovery(fresh, Instant.now()));

        return fresh;

    }

    private boolean isStale(final CachedDiscovery cached) {
        return Duration.between(cached.fetchedAt(), Instant.now()).getSeconds() > refreshIntervalSeconds;
    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public long getRefreshIntervalSeconds() {
        return refreshIntervalSeconds;
    }

    @Inject
    public void setRefreshIntervalSeconds(@Named(OIDC_DISCOVERY_REFRESH_SECONDS) long refreshIntervalSeconds) {
        this.refreshIntervalSeconds = refreshIntervalSeconds;
    }

}
