package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.application.ApplicationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * Created by patricktwohig on 7/10/15.
 */
public class ApplicationServiceProvider implements Provider<ApplicationService> {

    private User user;

    private Provider<AnonApplicationService> anonApplicationServiceProvider;

    private Provider<SuperUserApplicationService> superUserApplicationServiceProvider;

    @Override
    public ApplicationService get() {
        switch (getUser().getLevel()) {
        case SUPERUSER:
            return getSuperUserApplicationServiceProvider().get();
        default:
            return getAnonApplicationServiceProvider().get();

        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonApplicationService> getAnonApplicationServiceProvider() {
        return anonApplicationServiceProvider;
    }

    @Inject
    public void setAnonApplicationServiceProvider(Provider<AnonApplicationService> anonApplicationServiceProvider) {
        this.anonApplicationServiceProvider = anonApplicationServiceProvider;
    }

    public Provider<SuperUserApplicationService> getSuperUserApplicationServiceProvider() {
        return superUserApplicationServiceProvider;
    }

    @Inject
    public void setSuperUserApplicationServiceProvider(Provider<SuperUserApplicationService> superUserApplicationServiceProvider) {
        this.superUserApplicationServiceProvider = superUserApplicationServiceProvider;
    }

}
