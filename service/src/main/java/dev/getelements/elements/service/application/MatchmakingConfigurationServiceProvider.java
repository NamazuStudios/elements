package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.application.MatchmakingApplicationConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class MatchmakingConfigurationServiceProvider implements Provider<MatchmakingApplicationConfigurationService> {

    private User user;

    private Provider<SuperUserMatchmakingApplicationConfigurationService> superUserMatchmakingApplicationConfigurationServiceProvider;

    @Override
    public MatchmakingApplicationConfigurationService get() {
        switch (user.getLevel()) {
            case SUPERUSER: return getSuperUserMatchmakingApplicationConfigurationServiceProvider().get();
            default:        return forbidden(MatchmakingApplicationConfigurationService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserMatchmakingApplicationConfigurationService> getSuperUserMatchmakingApplicationConfigurationServiceProvider() {
        return superUserMatchmakingApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setSuperUserMatchmakingApplicationConfigurationServiceProvider(Provider<SuperUserMatchmakingApplicationConfigurationService> superUserMatchmakingApplicationConfigurationServiceProvider) {
        this.superUserMatchmakingApplicationConfigurationServiceProvider = superUserMatchmakingApplicationConfigurationServiceProvider;
    }

}
