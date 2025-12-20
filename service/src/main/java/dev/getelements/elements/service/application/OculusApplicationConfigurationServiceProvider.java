package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.application.OculusApplicationConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class OculusApplicationConfigurationServiceProvider  implements Provider<OculusApplicationConfigurationService> {

    private User user;

    private Provider<AnonOculusApplicationConfigurationService> anonOculusApplicationConfigurationServiceProvider;

    private Provider<SuperUserOculusApplicationConfigurationService> superUserOculusApplicationConfigurationServiceProvider;

    @Override
    public OculusApplicationConfigurationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserOculusApplicationConfigurationServiceProvider().get();
            default:
                return getAnonOculusApplicationConfigurationServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonOculusApplicationConfigurationService> getAnonOculusApplicationConfigurationServiceProvider() {
        return anonOculusApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setAnonOculusApplicationConfigurationServiceProvider(Provider<AnonOculusApplicationConfigurationService> anonOculusApplicationConfigurationServiceProvider) {
        this.anonOculusApplicationConfigurationServiceProvider = anonOculusApplicationConfigurationServiceProvider;
    }

    public Provider<SuperUserOculusApplicationConfigurationService> getSuperUserOculusApplicationConfigurationServiceProvider() {
        return superUserOculusApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setSuperUserOculusApplicationConfigurationServiceProvider(Provider<SuperUserOculusApplicationConfigurationService> superUserOculusApplicationConfigurationServiceProvider) {
        this.superUserOculusApplicationConfigurationServiceProvider = superUserOculusApplicationConfigurationServiceProvider;
    }

}