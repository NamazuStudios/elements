package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.TotpConfiguration;

import java.util.Optional;

/**
 * DAO for the single, system-wide {@link TotpConfiguration}, admin-managed via SUPERUSER CRUD. There is
 * exactly one configuration document, not a named collection.
 */
@ElementServiceExport
public interface TotpConfigurationDao {

    /**
     * Finds the TOTP configuration, if one has ever been set.
     *
     * @return an {@link Optional} containing the configuration, or empty if none has been configured
     */
    Optional<TotpConfiguration> findConfiguration();

    /**
     * Fetches the TOTP configuration, defaulting to disabled if none has ever been configured. Unlike a
     * secret-bearing configuration, there is nothing to distinguish "never configured" from "explicitly
     * disabled" here, so no not-found signal is needed.
     *
     * @return the configuration, never null
     */
    default TotpConfiguration getConfiguration() {
        return findConfiguration().orElseGet(() -> {
            final var configuration = new TotpConfiguration();
            configuration.setEnabled(false);
            return configuration;
        });
    }

    /**
     * Creates or updates the single TOTP configuration.
     *
     * @param configuration the configuration to persist
     * @return the persisted configuration
     */
    TotpConfiguration updateConfiguration(TotpConfiguration configuration);

}
