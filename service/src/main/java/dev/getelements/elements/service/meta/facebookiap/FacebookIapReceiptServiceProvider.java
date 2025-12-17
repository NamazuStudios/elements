package dev.getelements.elements.service.facebookiap;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.meta.facebookiap.FacebookIapReceiptService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class FacebookIapReceiptServiceProvider implements Provider<FacebookIapReceiptService> {

    private User user;

    private Provider<UserFacebookIapReceiptService> userFacebookIapReceiptServiceProvider;

    @Override
    public FacebookIapReceiptService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:
                return getUserFacebookIapReceiptServiceProvider().get();
            default:
                return Services.forbidden(FacebookIapReceiptService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserFacebookIapReceiptService> getUserFacebookIapReceiptServiceProvider() {
        return userFacebookIapReceiptServiceProvider;
    }

    @Inject
    public void setUserFacebookIapReceiptServiceProvider(Provider<UserFacebookIapReceiptService> userFacebookIapReceiptServiceProvider) {
        this.userFacebookIapReceiptServiceProvider = userFacebookIapReceiptServiceProvider;
    }
}
