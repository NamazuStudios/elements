package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.dao.OidcAuthSchemeDao;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Resolves an {@link OidcProviderConfiguration}'s discovery document and finds-or-creates the matching
 * {@link OidcAuthScheme} by issuer, so that registering a single provider configuration is enough to fully
 * activate a provider — no separate manual auth scheme setup step is required.
 */
public class OidcProviderConfigurationOperations {

    private OidcAuthSchemeDao oidcAuthSchemeDao;

    private OidcDiscoveryCache discoveryCache;

    /**
     * Resolves the discovery document for the given configuration, serving from the in-memory cache when
     * possible. The cache handles staleness/refresh transparently.
     *
     * @param config the provider configuration
     * @return the resolved discovery document
     */
    public OidcDiscoveryDocument resolveDiscovery(final OidcProviderConfiguration config) {
        return getDiscoveryCache().resolve(config.getDiscoveryUrl());
    }

    /**
     * Finds the {@link OidcAuthScheme} matching the discovery document's issuer, creating one (seeded with the
     * document's JWKS URI) if none exists yet. The actual JWKS key material is still fetched lazily by the
     * existing JWKS cache-on-miss logic the first time an unknown {@code kid} is seen.
     *
     * @param config the provider configuration
     * @param discoveryDocument the already-resolved discovery document for this configuration
     * @return the resolved or newly-created scheme
     */
    public OidcAuthScheme resolveScheme(final OidcProviderConfiguration config,
                                         final OidcDiscoveryDocument discoveryDocument) {

        final var issuer = discoveryDocument.getIssuer();

        return getOidcAuthSchemeDao()
                .findAuthScheme(issuer)
                .orElseGet(() -> createScheme(config, discoveryDocument));

    }

    private OidcAuthScheme createScheme(final OidcProviderConfiguration config,
                                         final OidcDiscoveryDocument discoveryDocument) {

        final var scheme = new OidcAuthScheme();
        scheme.setName(config.getName());
        scheme.setIssuer(discoveryDocument.getIssuer());
        scheme.setKeysUrl(discoveryDocument.getJwksUri());
        // keys is @NotNull and is read via scheme.getKeys().stream() before ever being fetched -- must start
        // as an empty list, not null, so validation passes and the lazy cache-on-miss fetch has something to miss.
        scheme.setKeys(List.of());

        return getOidcAuthSchemeDao().createAuthScheme(scheme);

    }

    public OidcAuthSchemeDao getOidcAuthSchemeDao() {
        return oidcAuthSchemeDao;
    }

    @Inject
    public void setOidcAuthSchemeDao(OidcAuthSchemeDao oidcAuthSchemeDao) {
        this.oidcAuthSchemeDao = oidcAuthSchemeDao;
    }

    public OidcDiscoveryCache getDiscoveryCache() {
        return discoveryCache;
    }

    @Inject
    public void setDiscoveryCache(OidcDiscoveryCache discoveryCache) {
        this.discoveryCache = discoveryCache;
    }

}
