package dev.getelements.elements.service.steam;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.steam.SteamIapReceiptService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class SteamIapReceiptServiceProvider implements Provider<SteamIapReceiptService> {

    private User user;

    private Provider<UserSteamIapReceiptService> userSteamIapReceiptServiceProvider;

    @Override
    public SteamIapReceiptService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:
                return getUserSteamIapReceiptServiceProvider().get();
            default:
                return Services.forbidden(SteamIapReceiptService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserSteamIapReceiptService> getUserSteamIapReceiptServiceProvider() {
        return userSteamIapReceiptServiceProvider;
    }

    @Inject
    public void setUserSteamIapReceiptServiceProvider(
            Provider<UserSteamIapReceiptService> userSteamIapReceiptServiceProvider
    ) {
        this.userSteamIapReceiptServiceProvider = userSteamIapReceiptServiceProvider;
    }

}
