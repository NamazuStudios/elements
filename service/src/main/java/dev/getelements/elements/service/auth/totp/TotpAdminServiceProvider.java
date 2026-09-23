package dev.getelements.elements.service.auth.totp;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.TotpAdminService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.service.Services.forbidden;

public class TotpAdminServiceProvider implements Provider<TotpAdminService> {

    private User user;

    private Provider<SuperUserTotpAdminService> totpAdminService;

    @Override
    public TotpAdminService get() {

        if (SUPERUSER.equals(user.getLevel())) {
            return getTotpAdminService().get();
        }

        return forbidden(TotpAdminService.class);

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserTotpAdminService> getTotpAdminService() {
        return totpAdminService;
    }

    @Inject
    public void setTotpAdminService(Provider<SuperUserTotpAdminService> totpAdminService) {
        this.totpAdminService = totpAdminService;
    }

}
