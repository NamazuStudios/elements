package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.CaptchaConfiguration;
import dev.getelements.elements.sdk.model.exception.auth.CaptchaConfigurationNotFoundException;

import java.util.Optional;

/**
 * DAO for the single, system-wide {@link CaptchaConfiguration}, admin-managed via SUPERUSER CRUD. Unlike
 * {@link OidcProviderConfigurationDao}, there is exactly one configuration document, not a named collection.
 */
@ElementServiceExport
public interface CaptchaConfigurationDao {

    /**
     * Finds the CAPTCHA configuration, if one has ever been set.
     *
     * @return an {@link Optional} containing the configuration, or empty if none has been configured
     */
    Optional<CaptchaConfiguration> findConfiguration();

    /**
     * Fetches the CAPTCHA configuration, throwing if none has been configured.
     *
     * @return the configuration, never null
     */
    default CaptchaConfiguration getConfiguration() {
        return findConfiguration().orElseThrow(CaptchaConfigurationNotFoundException::new);
    }

    /**
     * Creates or updates the single CAPTCHA configuration.
     *
     * @param configuration the configuration to persist
     * @return the persisted configuration
     */
    CaptchaConfiguration updateConfiguration(CaptchaConfiguration configuration);

}
