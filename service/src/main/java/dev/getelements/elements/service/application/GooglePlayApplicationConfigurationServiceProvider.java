package dev.getelements.elements.service.application;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.GooglePlayApplicationConfigurationService;

import javax.inject.Inject;
import javax.inject.Provider;


public class GooglePlayApplicationConfigurationServiceProvider implements Provider<GooglePlayApplicationConfigurationService> {

    private User user;

    private Provider<AnonGooglePlayApplicationConfigurationService> anonGooglePlayApplicationConfigurationServiceProvider;

    private Provider<SuperUserGooglePlayApplicationConfigurationService> superUserGooglePlayApplicationConfigurationServiceProvider;

    @Override
    public GooglePlayApplicationConfigurationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserGooglePlayApplicationConfigurationServiceProvider().get();
            default:
                return getAnonGooglePlayApplicationConfigurationServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonGooglePlayApplicationConfigurationService> getAnonGooglePlayApplicationConfigurationServiceProvider() {
        return anonGooglePlayApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setAnonGooglePlayApplicationConfigurationServiceProvider(
            Provider<AnonGooglePlayApplicationConfigurationService> anonGooglePlayApplicationConfigurationServiceProvider
    ) {
        this.anonGooglePlayApplicationConfigurationServiceProvider = anonGooglePlayApplicationConfigurationServiceProvider;
    }

    public Provider<SuperUserGooglePlayApplicationConfigurationService> getSuperUserGooglePlayApplicationConfigurationServiceProvider() {
        return superUserGooglePlayApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setSuperUserGooglePlayApplicationConfigurationServiceProvider(
            Provider<SuperUserGooglePlayApplicationConfigurationService> superUserGooglePlayApplicationConfigurationServiceProvider
    ) {
        this.superUserGooglePlayApplicationConfigurationServiceProvider = superUserGooglePlayApplicationConfigurationServiceProvider;
    }
}
