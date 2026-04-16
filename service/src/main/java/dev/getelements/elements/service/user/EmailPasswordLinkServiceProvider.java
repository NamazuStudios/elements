package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.user.EmailPasswordLinkService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class EmailPasswordLinkServiceProvider implements Provider<EmailPasswordLinkService> {

    private User user;

    private Provider<AnonEmailPasswordLinkService> anonServiceProvider;

    private Provider<UserEmailPasswordLinkService> userServiceProvider;

    @Override
    public EmailPasswordLinkService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserServiceProvider().get();
            default:
                return getAnonServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonEmailPasswordLinkService> getAnonServiceProvider() {
        return anonServiceProvider;
    }

    @Inject
    public void setAnonServiceProvider(Provider<AnonEmailPasswordLinkService> anonServiceProvider) {
        this.anonServiceProvider = anonServiceProvider;
    }

    public Provider<UserEmailPasswordLinkService> getUserServiceProvider() {
        return userServiceProvider;
    }

    @Inject
    public void setUserServiceProvider(Provider<UserEmailPasswordLinkService> userServiceProvider) {
        this.userServiceProvider = userServiceProvider;
    }

}
