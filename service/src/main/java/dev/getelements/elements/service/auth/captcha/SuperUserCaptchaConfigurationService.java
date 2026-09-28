package dev.getelements.elements.service.auth.captcha;

import dev.getelements.elements.sdk.dao.CaptchaConfigurationDao;
import dev.getelements.elements.sdk.model.auth.CaptchaConfiguration;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateCaptchaConfigurationRequest;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.CaptchaConfigurationService;
import jakarta.inject.Inject;

public class SuperUserCaptchaConfigurationService implements CaptchaConfigurationService {

    private CaptchaConfigurationDao captchaConfigurationDao;

    private ValidationHelper validationHelper;

    @Override
    public CaptchaConfiguration getConfiguration() {
        return redact(getCaptchaConfigurationDao().getConfiguration());
    }

    @Override
    public CaptchaConfiguration updateConfiguration(final CreateOrUpdateCaptchaConfigurationRequest request) {

        getValidationHelper().validateModel(request);

        final var existing = getCaptchaConfigurationDao().findConfiguration();

        final var config = new CaptchaConfiguration();
        config.setProvider(request.getProvider());
        config.setSiteKey(request.getSiteKey());
        config.setEnabled(request.isEnabled());

        if (request.getSecretKey() == null || request.getSecretKey().isBlank()) {
            // Blank means "leave unchanged" on update; the secret key is never readable back through the API,
            // so the caller has no way to resupply the existing value.
            if (existing.isEmpty()) {
                throw new InvalidParameterException("secretKey is required when configuring CAPTCHA for the first time.");
            }
            config.setSecretKey(existing.get().getSecretKey());
        } else {
            config.setSecretKey(request.getSecretKey());
        }

        return redact(getCaptchaConfigurationDao().updateConfiguration(config));

    }

    private CaptchaConfiguration redact(final CaptchaConfiguration config) {
        // The secret key authenticates this server to the provider; it must never be readable back through the
        // API once set, regardless of caller privilege.
        config.setSecretKey(null);
        return config;
    }

    public CaptchaConfigurationDao getCaptchaConfigurationDao() {
        return captchaConfigurationDao;
    }

    @Inject
    public void setCaptchaConfigurationDao(CaptchaConfigurationDao captchaConfigurationDao) {
        this.captchaConfigurationDao = captchaConfigurationDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
