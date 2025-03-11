package dev.getelements.elements.service.appleiap;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class AppleIapReceiptServiceProvider implements Provider<AppleIapReceiptService> {

    private User user;

    private Provider<UserAppleIapReceiptService> userAppleIapReceiptServiceProvider;

    @Override
    public AppleIapReceiptService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:
                return getUserAppleIapReceiptServiceProvider().get();
            default:
                return Services.forbidden(AppleIapReceiptService.class);
       }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserAppleIapReceiptService> getUserAppleIapReceiptServiceProvider() {
        return userAppleIapReceiptServiceProvider;
    }

    @Inject
    public void setUserAppleIapReceiptServiceProvider(Provider<UserAppleIapReceiptService> userAppleIapReceiptServiceProvider) {
        this.userAppleIapReceiptServiceProvider = userAppleIapReceiptServiceProvider;
    }
}
