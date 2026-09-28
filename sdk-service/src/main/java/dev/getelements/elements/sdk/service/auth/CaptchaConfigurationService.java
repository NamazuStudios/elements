package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.CaptchaConfiguration;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateCaptchaConfigurationRequest;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/** SUPERUSER-scoped CRUD service for the single, system-wide {@link CaptchaConfiguration}. */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface CaptchaConfigurationService {

    /**
     * Fetches the CAPTCHA configuration. If not found, an exception is raised.
     *
     * @return the {@link CaptchaConfiguration}, never null, with the secret key cleared
     */
    CaptchaConfiguration getConfiguration();

    /**
     * Creates or updates the CAPTCHA configuration.
     *
     * @param request the request with the information to create or update the configuration
     * @return the persisted configuration, with the secret key cleared
     */
    CaptchaConfiguration updateConfiguration(CreateOrUpdateCaptchaConfigurationRequest request);

}
