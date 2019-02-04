package com.namazustudios.socialengine.service.appleiap;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class AppleIapServiceProvider implements Provider<AppleIapService> {

    private User user;

    private Provider<AppleIapService> appleIapServiceProvider;

    @Override
    public AppleIapService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:
                return getAppleIapServiceProvider().get();
            default:
                return Services.forbidden(AppleIapService.class);
       }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AppleIapService> getAppleIapServiceProvider() {
        return appleIapServiceProvider;
    }

    @Inject
    public void setAppleIapServiceProvider(Provider<AppleIapService> appleIapServiceProvider) {
        this.appleIapServiceProvider = appleIapServiceProvider;
    }

}
