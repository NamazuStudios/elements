package dev.getelements.elements.service;

import dev.getelements.elements.service.auth.oidc.OidcDiscoveryCache;
import dev.getelements.elements.service.auth.oidc.OidcDiscoveryDocument;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

public class OidcDiscoveryCacheTest {

    private static final String DISCOVERY_URL = "https://id.twitch.tv/oauth2/.well-known/openid-configuration";

    private Client client;

    private WebTarget target;

    private Invocation.Builder requestBuilder;

    private OidcDiscoveryCache cache;

    @BeforeMethod
    public void setup() {

        client = mock(Client.class);
        target = mock(WebTarget.class);
        requestBuilder = mock(Invocation.Builder.class);

        when(client.target(DISCOVERY_URL)).thenReturn(target);
        when(target.request(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.get(OidcDiscoveryDocument.class)).thenAnswer(i -> new OidcDiscoveryDocument());

        cache = new OidcDiscoveryCache();
        cache.setClient(client);

    }

    @Test
    public void testServesFromCacheWithinRefreshWindow() {

        cache.setRefreshIntervalSeconds(3600);

        final var first = cache.resolve(DISCOVERY_URL);
        final var second = cache.resolve(DISCOVERY_URL);

        assertSame(first, second, "Second call within the refresh window should be served from cache");
        verify(client, times(1)).target(DISCOVERY_URL);

    }

    @Test
    public void testRefetchesOnceStale() {

        // A negative refresh interval makes every entry immediately stale, forcing a refetch on every call
        // without needing to sleep past a real TTL window.
        cache.setRefreshIntervalSeconds(-1);

        cache.resolve(DISCOVERY_URL);
        cache.resolve(DISCOVERY_URL);

        verify(client, times(2)).target(DISCOVERY_URL);

    }

}
