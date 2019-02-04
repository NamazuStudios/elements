package com.namazustudios.socialengine.service.appleiap;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class AppleIapReceiptServiceProvider implements Provider<AppleIapReceiptService> {

    private User user;

    private Provider<AppleIapReceiptService> appleIapServiceProvider;

    @Override
    public AppleIapReceiptService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:
                return getAppleIapServiceProvider().get();
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

    public Provider<AppleIapReceiptService> getAppleIapServiceProvider() {
        return appleIapServiceProvider;
    }

    @Inject
    public void setAppleIapServiceProvider(Provider<AppleIapReceiptService> appleIapServiceProvider) {
        this.appleIapServiceProvider = appleIapServiceProvider;
    }

}
