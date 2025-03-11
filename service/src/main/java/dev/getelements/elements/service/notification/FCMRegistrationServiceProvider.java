package dev.getelements.elements.service.notification;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.firebase.FCMRegistrationService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class FCMRegistrationServiceProvider implements Provider<FCMRegistrationService> {

    private User user;

    private Provider<UserFCMRegistrationService> userFCMRegistrationServiceProvider;

    private Provider<SuperUserFCMRegistrationService> superUserFCMRegistrationServiceProvider;

    @Override
    public FCMRegistrationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER: return getSuperUserFCMRegistrationServiceProvider().get();
            case USER: return getUserFCMRegistrationServiceProvider().get();
            default: return Services.forbidden(FCMRegistrationService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserFCMRegistrationService> getUserFCMRegistrationServiceProvider() {
        return userFCMRegistrationServiceProvider;
    }

    @Inject
    public void setUserFCMRegistrationServiceProvider(Provider<UserFCMRegistrationService> userFCMRegistrationServiceProvider) {
        this.userFCMRegistrationServiceProvider = userFCMRegistrationServiceProvider;
    }

    public Provider<SuperUserFCMRegistrationService> getSuperUserFCMRegistrationServiceProvider() {
        return superUserFCMRegistrationServiceProvider;
    }

    @Inject
    public void setSuperUserFCMRegistrationServiceProvider(Provider<SuperUserFCMRegistrationService> superUserFCMRegistrationServiceProvider) {
        this.superUserFCMRegistrationServiceProvider = superUserFCMRegistrationServiceProvider;
    }

}
