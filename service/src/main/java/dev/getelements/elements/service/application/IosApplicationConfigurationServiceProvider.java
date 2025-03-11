package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.application.IosApplicationConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;


public class IosApplicationConfigurationServiceProvider implements Provider<IosApplicationConfigurationService> {

    private User user;

    private Provider<AnonIosApplicationConfigurationService> anonIosApplicationConfigurationServiceProvider;

    private Provider<SuperUserIosApplicationConfigurationService> superUserIosApplicationConfigurationServiceProvider;

    @Override
    public IosApplicationConfigurationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserIosApplicationConfigurationServiceProvider().get();
            default:
                return getAnonIosApplicationConfigurationServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonIosApplicationConfigurationService> getAnonIosApplicationConfigurationServiceProvider() {
        return anonIosApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setAnonIosApplicationConfigurationServiceProvider(
            Provider<AnonIosApplicationConfigurationService> anonIosApplicationConfigurationServiceProvider
    ) {
        this.anonIosApplicationConfigurationServiceProvider = anonIosApplicationConfigurationServiceProvider;
    }

    public Provider<SuperUserIosApplicationConfigurationService> getSuperUserIosApplicationConfigurationServiceProvider() {
        return superUserIosApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setSuperUserIosApplicationConfigurationServiceProvider(
            Provider<SuperUserIosApplicationConfigurationService> superUserIosApplicationConfigurationServiceProvider
    ) {
        this.superUserIosApplicationConfigurationServiceProvider = superUserIosApplicationConfigurationServiceProvider;
    }
}
