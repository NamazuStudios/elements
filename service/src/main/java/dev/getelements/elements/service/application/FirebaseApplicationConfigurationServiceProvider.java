package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.application.FirebaseApplicationConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class FirebaseApplicationConfigurationServiceProvider implements Provider<FirebaseApplicationConfigurationService> {

    private User user;

    private Provider<SuperUserFirebaseApplicationConfigurationService> superUserFirebaseApplicationConfigurationServiceProvider;

    @Override
    public FirebaseApplicationConfigurationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER: return getSuperUserFirebaseApplicationConfigurationServiceProvider().get();
            default:        return forbidden(FirebaseApplicationConfigurationService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserFirebaseApplicationConfigurationService> getSuperUserFirebaseApplicationConfigurationServiceProvider() {
        return superUserFirebaseApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setSuperUserFirebaseApplicationConfigurationServiceProvider(Provider<SuperUserFirebaseApplicationConfigurationService> superUserFirebaseApplicationConfigurationServiceProvider) {
        this.superUserFirebaseApplicationConfigurationServiceProvider = superUserFirebaseApplicationConfigurationServiceProvider;
    }

}
