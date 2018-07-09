package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.GameOnApplicationConfigurationService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class GameOnApplicationConfigurationServiceProvider implements Provider<GameOnApplicationConfigurationService> {

    private User user;

    private Provider<AnonGameOnApplicationConfigurationService> anonGameOnApplicationConfigurationServiceProvider;

    private Provider<SuperUserGameOnApplicationConfigurationService> superUserGameOnApplicationConfigurationServiceProvider;

    @Override
    public GameOnApplicationConfigurationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER: return getSuperUserGameOnApplicationConfigurationServiceProvider().get();
            case USER:
            case UNPRIVILEGED: return getAnonGameOnApplicationConfigurationServiceProvider().get();
            default: return forbidden(GameOnApplicationConfigurationService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonGameOnApplicationConfigurationService> getAnonGameOnApplicationConfigurationServiceProvider() {
        return anonGameOnApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setAnonGameOnApplicationConfigurationServiceProvider(Provider<AnonGameOnApplicationConfigurationService> anonGameOnApplicationConfigurationServiceProvider) {
        this.anonGameOnApplicationConfigurationServiceProvider = anonGameOnApplicationConfigurationServiceProvider;
    }

    public Provider<SuperUserGameOnApplicationConfigurationService> getSuperUserGameOnApplicationConfigurationServiceProvider() {
        return superUserGameOnApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setSuperUserGameOnApplicationConfigurationServiceProvider(Provider<SuperUserGameOnApplicationConfigurationService> superUserGameOnApplicationConfigurationServiceProvider) {
        this.superUserGameOnApplicationConfigurationServiceProvider = superUserGameOnApplicationConfigurationServiceProvider;
    }

}
