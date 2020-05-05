package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.MatchmakingApplicationConfigurationService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

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
