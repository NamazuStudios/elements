package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateTotpConfigurationRequest;
import dev.getelements.elements.sdk.model.auth.TotpConfiguration;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/** SUPERUSER-scoped CRUD service for the single, system-wide {@link TotpConfiguration}. */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface TotpConfigurationService {

    /**
     * Fetches the TOTP configuration, defaulting to disabled if never configured.
     *
     * @return the {@link TotpConfiguration}, never null
     */
    TotpConfiguration getConfiguration();

    /**
     * Creates or updates the TOTP configuration.
     *
     * @param request the request with the information to create or update the configuration
     * @return the persisted configuration
     */
    TotpConfiguration updateConfiguration(CreateOrUpdateTotpConfigurationRequest request);

}
