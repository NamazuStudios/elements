package dev.getelements.elements.service.application;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.ApplicationConfigurationService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Selects the appropriate {@link ApplicationConfigurationService} based on  the {@link User#getLevel()} property.
 *
 * Created by patricktwohig on 7/13/15.
 */
public class ApplicationConfigurationServiceProvider implements Provider<ApplicationConfigurationService> {

    private User user;

    private Provider<AnonApplicationConfigurationService> anonApplicationConfigurationServiceProvider;

    private Provider<SuperUserApplicationConfigurationService> superUserApplicationProfileServiceProvider;

    @Override
    public ApplicationConfigurationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserApplicationProfileServiceProvider().get();
            default:
                return getAnonApplicationConfigurationServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonApplicationConfigurationService> getAnonApplicationConfigurationServiceProvider() {
        return anonApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setAnonApplicationConfigurationServiceProvider(Provider<AnonApplicationConfigurationService> anonApplicationConfigurationServiceProvider) {
        this.anonApplicationConfigurationServiceProvider = anonApplicationConfigurationServiceProvider;
    }

    public Provider<SuperUserApplicationConfigurationService> getSuperUserApplicationProfileServiceProvider() {
        return superUserApplicationProfileServiceProvider;
    }

    @Inject
    public void setSuperUserApplicationProfileServiceProvider(Provider<SuperUserApplicationConfigurationService> superUserApplicationProfileServiceProvider) {
        this.superUserApplicationProfileServiceProvider = superUserApplicationProfileServiceProvider;
    }

}
