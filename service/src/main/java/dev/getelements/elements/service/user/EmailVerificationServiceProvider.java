package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.user.EmailVerificationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class EmailVerificationServiceProvider implements Provider<EmailVerificationService> {

    private User user;

    private Provider<UserEmailVerificationService> userServiceProvider;

    private Provider<SuperUserEmailVerificationService> superUserServiceProvider;

    @Override
    public EmailVerificationService get() {
        switch (getUser().getLevel()) {
            case USER:
                return getUserServiceProvider().get();
            case SUPERUSER:
                return getSuperUserServiceProvider().get();
            default:
                throw new ForbiddenException("Anonymous users may not initiate email verification.");
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserEmailVerificationService> getUserServiceProvider() {
        return userServiceProvider;
    }

    @Inject
    public void setUserServiceProvider(Provider<UserEmailVerificationService> userServiceProvider) {
        this.userServiceProvider = userServiceProvider;
    }

    public Provider<SuperUserEmailVerificationService> getSuperUserServiceProvider() {
        return superUserServiceProvider;
    }

    @Inject
    public void setSuperUserServiceProvider(Provider<SuperUserEmailVerificationService> superUserServiceProvider) {
        this.superUserServiceProvider = superUserServiceProvider;
    }

}
