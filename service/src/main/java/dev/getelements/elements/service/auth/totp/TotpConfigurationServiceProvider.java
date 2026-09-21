package dev.getelements.elements.service.auth.totp;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.TotpConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.service.Services.forbidden;

public class TotpConfigurationServiceProvider implements Provider<TotpConfigurationService> {

    private User user;

    private Provider<SuperUserTotpConfigurationService> totpConfigurationService;

    @Override
    public TotpConfigurationService get() {

        if (SUPERUSER.equals(user.getLevel())) {
            return getTotpConfigurationService().get();
        }

        return forbidden(TotpConfigurationService.class);

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserTotpConfigurationService> getTotpConfigurationService() {
        return totpConfigurationService;
    }

    @Inject
    public void setTotpConfigurationService(Provider<SuperUserTotpConfigurationService> totpConfigurationService) {
        this.totpConfigurationService = totpConfigurationService;
    }

}
