package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.user.UsernamePasswordLinkService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class UsernamePasswordLinkServiceProvider implements Provider<UsernamePasswordLinkService> {

    private User user;

    private Provider<UserUsernamePasswordLinkService> userServiceProvider;

    @Override
    public UsernamePasswordLinkService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserServiceProvider().get();
            default:
                throw new ForbiddenException("Authentication required to link credentials.");
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserUsernamePasswordLinkService> getUserServiceProvider() {
        return userServiceProvider;
    }

    @Inject
    public void setUserServiceProvider(Provider<UserUsernamePasswordLinkService> userServiceProvider) {
        this.userServiceProvider = userServiceProvider;
    }

}
