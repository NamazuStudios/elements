package dev.getelements.elements.service.meta.oculusiap;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.meta.oculusiap.OculusIapReceiptService;
import dev.getelements.elements.service.util.Services;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class OculusIapReceiptServiceProvider implements Provider<OculusIapReceiptService> {

    private User user;

    private Provider<UserOculusIapReceiptService> userOculusIapReceiptServiceProvider;

    @Override
    public OculusIapReceiptService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:
                return getUserOculusIapReceiptServiceProvider().get();
            default:
                return Services.forbidden(OculusIapReceiptService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserOculusIapReceiptService> getUserOculusIapReceiptServiceProvider() {
        return userOculusIapReceiptServiceProvider;
    }

    @Inject
    public void setUserOculusIapReceiptServiceProvider(Provider<UserOculusIapReceiptService> userOculusIapReceiptServiceProvider) {
        this.userOculusIapReceiptServiceProvider = userOculusIapReceiptServiceProvider;
    }
}
