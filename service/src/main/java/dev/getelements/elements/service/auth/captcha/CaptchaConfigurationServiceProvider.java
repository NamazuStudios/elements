package dev.getelements.elements.service.auth.captcha;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.CaptchaConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.service.Services.forbidden;

public class CaptchaConfigurationServiceProvider implements Provider<CaptchaConfigurationService> {

    private User user;

    private Provider<SuperUserCaptchaConfigurationService> captchaConfigurationService;

    @Override
    public CaptchaConfigurationService get() {

        if (SUPERUSER.equals(user.getLevel())) {
            return getCaptchaConfigurationService().get();
        }

        return forbidden(CaptchaConfigurationService.class);

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserCaptchaConfigurationService> getCaptchaConfigurationService() {
        return captchaConfigurationService;
    }

    @Inject
    public void setCaptchaConfigurationService(Provider<SuperUserCaptchaConfigurationService> captchaConfigurationService) {
        this.captchaConfigurationService = captchaConfigurationService;
    }

}
