package dev.getelements.elements.service.auth.captcha;

import dev.getelements.elements.sdk.dao.CaptchaConfigurationDao;
import dev.getelements.elements.sdk.model.auth.CaptchaConfiguration;
import dev.getelements.elements.sdk.model.auth.CaptchaPublicConfiguration;
import dev.getelements.elements.sdk.model.auth.CaptchaVerifyRequest;
import dev.getelements.elements.sdk.model.auth.CaptchaVerifyResponse;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.CaptchaService;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCaptchaService implements CaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCaptchaService.class);

    private static final String RECAPTCHA_SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private CaptchaConfigurationDao captchaConfigurationDao;

    private ValidationHelper validationHelper;

    private Client client;

    @Override
    public CaptchaPublicConfiguration getPublicConfiguration() {

        final var response = new CaptchaPublicConfiguration();

        getCaptchaConfigurationDao()
                .findConfiguration()
                .filter(CaptchaConfiguration::isEnabled)
                .ifPresent(config -> {
                    response.setEnabled(true);
                    response.setProvider(config.getProvider());
                    response.setSiteKey(config.getSiteKey());
                });

        return response;

    }

    @Override
    public CaptchaVerifyResponse verify(final CaptchaVerifyRequest request) {

        getValidationHelper().validateModel(request);

        final var response = new CaptchaVerifyResponse();

        final var config = getCaptchaConfigurationDao().findConfiguration().orElse(null);

        if (config == null || !config.isEnabled()) {
            response.setSuccess(false);
            return response;
        }

        return switch (config.getProvider()) {
            case RECAPTCHA -> verifyRecaptcha(config, request.getToken());
        };

    }

    private CaptchaVerifyResponse verifyRecaptcha(final CaptchaConfiguration config, final String token) {

        final var response = new CaptchaVerifyResponse();

        final var form = new Form()
                .param("secret", config.getSecretKey())
                .param("response", token);

        final var target = getClient().target(RECAPTCHA_SITE_VERIFY_URL);
        final var httpResponse = target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        try {

            if (httpResponse.getStatus() != 200) {
                logger.warn("reCAPTCHA siteverify returned unexpected status {}", httpResponse.getStatus());
                response.setSuccess(false);
                return response;
            }

            final var siteVerifyResponse = httpResponse.readEntity(RecaptchaSiteVerifyResponse.class);
            response.setSuccess(siteVerifyResponse != null && siteVerifyResponse.isSuccess());
            return response;

        } finally {
            httpResponse.close();
        }

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

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
