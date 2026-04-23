package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.user.PasswordResetService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class PasswordResetServiceProvider implements Provider<PasswordResetService> {

    private User user;

    private Provider<AnonPasswordResetService> anonServiceProvider;

    @Override
    public PasswordResetService get() {
        return getAnonServiceProvider().get();
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonPasswordResetService> getAnonServiceProvider() {
        return anonServiceProvider;
    }

    @Inject
    public void setAnonServiceProvider(Provider<AnonPasswordResetService> anonServiceProvider) {
        this.anonServiceProvider = anonServiceProvider;
    }

}
