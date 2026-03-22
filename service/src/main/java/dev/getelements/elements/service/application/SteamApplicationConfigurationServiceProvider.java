package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.application.SteamApplicationConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class SteamApplicationConfigurationServiceProvider implements Provider<SteamApplicationConfigurationService> {

    private User user;

    private Provider<AnonSteamApplicationConfigurationService> anonSteamApplicationConfigurationServiceProvider;

    private Provider<SuperUserSteamApplicationConfigurationService> superUserSteamApplicationConfigurationServiceProvider;

    @Override
    public SteamApplicationConfigurationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserSteamApplicationConfigurationServiceProvider().get();
            default:
                return getAnonSteamApplicationConfigurationServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonSteamApplicationConfigurationService> getAnonSteamApplicationConfigurationServiceProvider() {
        return anonSteamApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setAnonSteamApplicationConfigurationServiceProvider(Provider<AnonSteamApplicationConfigurationService> anonSteamApplicationConfigurationServiceProvider) {
        this.anonSteamApplicationConfigurationServiceProvider = anonSteamApplicationConfigurationServiceProvider;
    }

    public Provider<SuperUserSteamApplicationConfigurationService> getSuperUserSteamApplicationConfigurationServiceProvider() {
        return superUserSteamApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setSuperUserSteamApplicationConfigurationServiceProvider(Provider<SuperUserSteamApplicationConfigurationService> superUserSteamApplicationConfigurationServiceProvider) {
        this.superUserSteamApplicationConfigurationServiceProvider = superUserSteamApplicationConfigurationServiceProvider;
    }

}
